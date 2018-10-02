package com.fitpay.android.api;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.callbacks.CallbackWrapper;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.PlatformConfig;
import com.fitpay.android.api.models.card.VerificationMethods;
import com.fitpay.android.api.models.device.ResetDeviceResult;
import com.fitpay.android.api.models.issuer.Issuers;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.api.models.user.LoginIdentity;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.models.user.UserCreateRequest;
import com.fitpay.android.api.services.AuthClient;
import com.fitpay.android.api.services.AuthService;
import com.fitpay.android.api.services.FitPayClient;
import com.fitpay.android.api.services.FitPayService;
import com.fitpay.android.api.services.UserClient;
import com.fitpay.android.api.services.UserService;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.KeysManager;
import com.fitpay.android.utils.ObjectConverter;
import com.fitpay.android.utils.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;

/*
 * API manager
 */
public class ApiManager {

    private static volatile ApiManager sInstance;

    public static ApiManager getInstance() {
        if (sInstance == null) {
            synchronized (ApiManager.class) {
                if (sInstance == null) {
                    sInstance = new ApiManager();
                }
            }
        }

        return sInstance;
    }

    /**
     * Clean API static variables. Required to be call in case of app URL overrides.
     */
    public static void clean() {
        if (sInstance != null) {
            synchronized (ApiManager.class) {
                sInstance = null;
            }
        }
    }

    private FitPayService apiService;
    private UserService userService;
    private AuthService authService;

    private ApiManager() {
        apiService = new FitPayService(FitpayConfig.apiURL);
    }

    public PlatformConfig getPlatformConfig() {
        return apiService.getPlatformConfig();
    }

    public FitPayService getApiService() {
        return apiService;
    }

    public void setAuthToken(OAuthToken token) {
        apiService.updateToken(token);
    }

    public FitPayClient getClient() {
        return apiService.getClient();
    }

    public AuthClient getAuthClient() {
        if (null == authService) {
            synchronized (this) {
                String baseUrl = FitpayConfig.authURL;
                if (null == FitpayConfig.clientId) {
                    throw new IllegalArgumentException("The configuration must contain the following property: "
                            + FitpayConfig.clientId);
                }
                authService = new AuthService(baseUrl);
            }
        }
        return authService.getClient();
    }

    public UserClient getUserClient() {
        if (null == userService) {
            synchronized (this) {
                userService = new UserService(FitpayConfig.apiURL);
            }
        }
        return userService.getClient();
    }

    public boolean isAuthorized(@NonNull ApiCallback callback) {
        if (!apiService.isAuthorized()) {
            callback.onFailure(ResultCode.UNAUTHORIZED, "Unauthorized");

            return false;
        }

        return true;
    }

    private void checkKeyAndMakeCall(@NonNull Runnable successRunnable, @NonNull ApiCallback callback) {
        if (KeysManager.getInstance().keyRequireUpdate(KeysManager.KEY_API)) {
            KeysManager.getInstance().updateECCKey(KeysManager.KEY_API, successRunnable, callback);
        } else {
            successRunnable.run();
        }
    }

    /**
     * User Creation
     *
     * @param user     user to build
     * @param callback result callback
     */
    public void createUser(UserCreateRequest user, final ApiCallback<User> callback) {

        Runnable onSuccess = () -> {
            user.addCredentials(FitpayConfig.clientId);
            Call<User> createUserCall = getUserClient().createUser(user);
            createUserCall.enqueue(new CallbackWrapper<>(callback));
        };

        checkKeyAndMakeCall(onSuccess, callback);
    }

    /**
     * User Login
     *
     * @param identity data for login
     * @param callback result callback
     */
    public void login(LoginIdentity identity, final ApiCallback<OAuthToken> callback) {

        CallbackWrapper<OAuthToken> updateTokenCallback = new CallbackWrapper<>(new ApiCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                if (null == result || result.getUserId() == null) {
                    callback.onFailure(ResultCode.UNAUTHORIZED, "user login was not successful");
                    return;
                }
                apiService.updateToken(result);
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                if (callback != null) {
                    callback.onFailure(errorCode, errorMessage);
                }
            }
        });
        Map<String, String> allParams = new HashMap<>();
        allParams.put("credentials", getCredentialsString(identity));
        allParams.put("response_type", "token");
        allParams.put("client_id", FitpayConfig.clientId);
        allParams.put("redirect_uri", FitpayConfig.redirectURL);
        Call<OAuthToken> getTokenCall = getAuthClient().loginCredentials(allParams);
        getTokenCall.enqueue(updateTokenCallback);
    }

    /**
     * User Login
     *
     * @param firebaseToken token for login
     * @param callback      result callback
     */
    public void login(String firebaseToken, final ApiCallback<OAuthToken> callback) {
        CallbackWrapper<OAuthToken> updateTokenCallback = new CallbackWrapper<>(new ApiCallback<OAuthToken>() {
            @Override
            public void onSuccess(OAuthToken result) {
                if (null == result || result.getUserId() == null) {
                    callback.onFailure(ResultCode.UNAUTHORIZED, "user login was not successful");
                    return;
                }
                apiService.updateToken(result);
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                if (callback != null) {
                    callback.onFailure(errorCode, errorMessage);
                }
            }
        });

        Map<String, String> allParams = new HashMap<>();
        allParams.put("firebase_token", firebaseToken);
        allParams.put("response_type", "token");
        allParams.put("client_id", FitpayConfig.clientId);
        allParams.put("redirect_uri", FitpayConfig.redirectURL);
        Call<OAuthToken> getTokenCall = getAuthClient().loginToken(allParams);

        getTokenCall.enqueue(updateTokenCallback);
    }

    protected String getCredentialsString(LoginIdentity identity) {

        return new StringBuilder()
                .append("{\"username\":\"")
                .append(identity.getUsername())
                .append("\",\"password\":\"")
                .append(identity.getPassword())
                .append("\"}")
                .toString();
    }

    /**
     * Retrieves the details of an existing user.
     * You need only supply the unique user identifier that was returned upon user creation.
     *
     * @param callback result callback
     */
    public void getUser(final ApiCallback<User> callback) {
        if (isAuthorized(callback)) {

            Runnable onSuccess = () -> {
                Call<User> getUserCall = getClient().getUser(apiService.getUserId());
                getUserCall.enqueue(new CallbackWrapper<>(callback));
            };

            checkKeyAndMakeCall(onSuccess, callback);
        }
    }

    /**
     * Retrieves the details of an existing user.
     * You need only supply the unique user identifier that was returned upon user creation.
     *
     * @param callback result callback
     */
    public void getIssuers(final ApiCallback<Issuers> callback) {
        Runnable onSuccess = () -> {
            Call<Issuers> getIssuersCall = getClient().getIssuers();
            getIssuersCall.enqueue(new CallbackWrapper<>(callback));
        };

        checkKeyAndMakeCall(onSuccess, callback);
    }

    /**
     * Provides a fresh list of available verification methods for the credit card
     *
     * @param userId       user id
     * @param creditCardId credit card id
     * @param callback     result callback
     */
    public void getVerificationMethods(String userId, String creditCardId, ApiCallback<VerificationMethods> callback) {
        if (isAuthorized(callback)) {
            Runnable onSuccess = () -> {
                Call<VerificationMethods> getVerificationMethodsCall = getClient().getVerificationMethods(userId, creditCardId);
                getVerificationMethodsCall.enqueue(new CallbackWrapper<>(callback));
            };
            checkKeyAndMakeCall(onSuccess, callback);
        }
    }

    /**
     * Provides the ability to initiate a reset of the secure element.
     * This will delete all tokens and re-initialize the device.
     * Calls the /resetDeviceTasks endpoint
     *
     * @param userId   user id
     * @param deviceId payment device id
     * @param callback result callback
     */
    public void resetPaymentDevice(@NonNull String userId, @NonNull String deviceId, final ApiCallback<ResetDeviceResult> callback) {
        if (isAuthorized(callback)) {
            Runnable onSuccess = () -> {
                Call<ResetDeviceResult> resetDeviceCall = getClient().resetPaymentDevice(userId, deviceId);
                resetDeviceCall.enqueue(new CallbackWrapper<>(callback));
            };

            checkKeyAndMakeCall(onSuccess, callback);
        }
    }

    /**
     * Get status for {@link ApiManager#resetPaymentDevice(String, String, ApiCallback)}
     *
     * @param resetId  reset id from {@link ResetDeviceResult#resetId}
     * @param callback result callback
     */
    public void getResetPaymentDeviceStatus(@NonNull String resetId,
                                            final ApiCallback<ResetDeviceResult> callback) {
        if (isAuthorized(callback)) {
            Runnable onSuccess = () -> {
                Call<ResetDeviceResult> resetDeviceCall = getClient().getResetPaymentDeviceStatus(resetId);
                resetDeviceCall.enqueue(new CallbackWrapper<>(callback));
            };

            checkKeyAndMakeCall(onSuccess, callback);
        }
    }

    private <T> void makeCall(final Call<JsonElement> call, final Type type, final ApiCallback<T> callback) {
        call.enqueue(new CallbackWrapper<>(new ApiCallbackExt<JsonElement>() {
            @Override
            public void onSuccess(JsonElement result) {
                T response = Constants.getGson().fromJson(result, type);
                callback.onSuccess(response);
            }

            @Override
            public void onFailure(ErrorResponse apiErrorResponse) {
                if (callback instanceof ApiCallbackExt) {
                    ((ApiCallbackExt) callback).onFailure(apiErrorResponse);
                } else {
                    callback.onFailure(apiErrorResponse.getStatus(), apiErrorResponse.getError());
                }
            }
        }));
    }

    public <T> void get(final String url, final Map<String, Object> queryMap, final Type type, final ApiCallback<T> callback) {
        Call<JsonElement> getDataCall = queryMap != null ? getClient().get(url, queryMap) : getClient().get(url);
        makeCall(getDataCall, type, callback);
    }

    public <T, U> void post(final String url, final U data, final Type type, final ApiCallback<T> callback) {
        Call<JsonElement> postDataCall = data != null ? getClient().post(url, data) : getClient().post(url);
        makeCall(postDataCall, type, callback);
    }

    public <U> void post(String url, final U data, final ApiCallback<Void> callback) {
        Call<Void> postDataCall = data != null ? getClient().postNoResponse(url, data) : getClient().postNoResponse(url);
        postDataCall.enqueue(new CallbackWrapper<>(callback));
    }

    public <T, U> void patch(final String url, final U data, final boolean add, final boolean encrypt, final Type type, final ApiCallback<T> callback) {
        JsonArray updateData = new JsonArray();

        Map<String, Object> userMap = ObjectConverter.convertToSimpleMap(data);
        for (Map.Entry<String, Object> entry : userMap.entrySet()) {
            JsonObject item = new JsonObject();
            item.addProperty("op", add ? "add" : "replace");
            item.addProperty("path", entry.getKey());
            item.addProperty("value", String.valueOf(entry.getValue()));

            updateData.add(item);
        }

        Call<JsonElement> patchDataCall;

        if (encrypt) {
            String userString = updateData.toString();

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("encryptedData", StringUtils.getEncryptedString(KeysManager.KEY_API, userString));

            patchDataCall = getClient().patch(url, jsonObject);
        } else {
            patchDataCall = getClient().patch(url, updateData);
        }

        makeCall(patchDataCall, type, callback);
    }

    public <T> void put(final String url, final T data, final Type type, final ApiCallback<T> callback) {
        Call<JsonElement> putDataCall = getClient().put(url, data);
        makeCall(putDataCall, type, callback);
    }

    public void delete(String url, final ApiCallback<Void> callback) {
        Call<Void> deleteDataCall = getClient().delete(url);
        deleteDataCall.enqueue(new CallbackWrapper<>(callback));
    }

}
