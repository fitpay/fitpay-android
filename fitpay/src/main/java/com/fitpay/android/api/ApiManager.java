package com.fitpay.android.api;

import android.support.annotation.NonNull;

import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.callbacks.CallbackWrapper;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.PlatformConfig;
import com.fitpay.android.api.models.Relationship;
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
import com.fitpay.android.utils.FPLog;
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
import retrofit2.Callback;
import retrofit2.Response;

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
        synchronized (ApiManager.class) {
            sInstance = null;
        }
    }

    private FitPayService apiService;
    private UserService userService;
    private AuthService authService;

    private ApiManager() {
        synchronized (this) {
            FitpayConfig fitpayConfig = FitpayConfig.getInstance();
            String apiUrl = fitpayConfig.getApiUrl();
            if (null == FitpayConfig.getInstance().getApiUrl()) {
                throw new IllegalStateException("The FitpayConfig must be initialized prior to use. API base url required");
            }
            apiService = new FitPayService(apiUrl);
            init(fitpayConfig.get(FitpayConfig.PROPERTY_SKIP_HEALTH_CHECK));
        }
    }

    public PlatformConfig getPlatformConfig() {
        return apiService.getPlatformConfig();
    }

    /**
     * Initialize the SDK, skipHealthCheck determines if the SDK will perform and initial health check
     * on the API or not after initialization.
     *
     * @param skipHealthCheck
     */
    public void init(boolean skipHealthCheck) {
        if (!skipHealthCheck) {
            Call<Object> healthCall = getClient().health();
            healthCall.enqueue(new Callback<Object>() {
                @Override
                public void onResponse(Call<Object> call, Response<Object> response) {
                    FPLog.i("FitPay API Health Result: " + response.body());
                }

                @Override
                public void onFailure(Call<Object> call, Throwable t) {
                    FPLog.e("FitPay API Health Check Failed: " + t.getMessage());
                    FPLog.e(t);
                }
            });
        }
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
                FitpayConfig fitpayConfig = FitpayConfig.getInstance();
                String baseUrl = fitpayConfig.get(FitpayConfig.PROPERTY_AUTH_BASE_URL);
                if (null == baseUrl) {
                    baseUrl = fitpayConfig.get(FitpayConfig.PROPERTY_API_BASE_URL);
                }
                if (null == baseUrl) {
                    throw new IllegalArgumentException("The configuration must contain one of the following two properties: "
                            + FitpayConfig.PROPERTY_AUTH_BASE_URL + " or " + FitpayConfig.PROPERTY_API_BASE_URL);
                }
                if (null == fitpayConfig.get(FitpayConfig.PROPERTY_CLIENT_ID)) {
                    throw new IllegalArgumentException("The configuration must contain the following property: "
                            + FitpayConfig.PROPERTY_CLIENT_ID);
                }
                if (null == fitpayConfig.get(FitpayConfig.PROPERTY_REDIRECT_URL)) {
                    throw new IllegalArgumentException("The configuration must contain the following property: "
                            + FitpayConfig.PROPERTY_REDIRECT_URL);
                }
                authService = new AuthService(baseUrl);
            }
        }
        return authService.getClient();
    }

    public UserClient getUserClient() {
        if (null == userService) {
            synchronized (this) {
                String apiUrl = FitpayConfig.getInstance().getApiUrl();
                if (null == FitpayConfig.getInstance().getApiUrl()) {
                    throw new IllegalStateException("The FitpayConfig must be initialized prior to use.  API base url required");
                }
                userService = new UserService(apiUrl);
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
            user.addCredentials(FitpayConfig.getInstance().get(FitpayConfig.PROPERTY_CLIENT_ID));
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
    public void loginUser(LoginIdentity identity, final ApiCallback<OAuthToken> callback) {

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
        allParams.put("client_id", FitpayConfig.getInstance().get(FitpayConfig.PROPERTY_CLIENT_ID));
        allParams.put("redirect_uri", FitpayConfig.getInstance().get(FitpayConfig.PROPERTY_REDIRECT_URL));
        Call<OAuthToken> getTokenCall = getAuthClient().loginUser(allParams);
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

            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    Call<User> getUserCall = getClient().getUser(apiService.getUserId());
                    getUserCall.enqueue(new CallbackWrapper<>(callback));
                }
            };

            checkKeyAndMakeCall(onSuccess, callback);
        }
    }


    /**
     * Creates a relationship between a device and a creditCard.
     *
     * @param userId       user id
     * @param creditCardId credit card id
     * @param deviceId     device id
     * @param callback     result callback
     */
    public void createRelationship(String userId, String creditCardId, String deviceId, ApiCallback<Relationship> callback) {
        if (isAuthorized(callback)) {

            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    Call<Relationship> createRelationshipCall = getClient().createRelationship(userId, creditCardId, deviceId);
                    createRelationshipCall.enqueue(new CallbackWrapper<>(callback));
                }
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
        Runnable onSuccess = new Runnable() {
            @Override
            public void run() {
                Call<Issuers> getIssuersCall = getClient().getIssuers();
                getIssuersCall.enqueue(new CallbackWrapper<>(callback));
            }
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
            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    Call<VerificationMethods> getVerificationMethodsCall = getClient().getVerificationMethods(userId, creditCardId);
                    getVerificationMethodsCall.enqueue(new CallbackWrapper<>(callback));
                }
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
            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    Call<ResetDeviceResult> resetDeviceCall = getClient().resetPaymentDevice(userId, deviceId);
                    resetDeviceCall.enqueue(new CallbackWrapper<>(callback));
                }
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
            Runnable onSuccess = new Runnable() {
                @Override
                public void run() {
                    Call<ResetDeviceResult> resetDeviceCall = getClient().getResetPaymentDeviceStatus(resetId);
                    resetDeviceCall.enqueue(new CallbackWrapper<>(callback));
                }
            };

            checkKeyAndMakeCall(onSuccess, callback);
        }
    }

//    /**
//     * Get a single relationship.
//     *
//     * @param userId       user id
//     * @param creditCardId credit card id
//     * @param deviceId     device id
//     * @param callback     result callback
//     */
//    public void getRelationship(String userId, String creditCardId, String deviceId, ApiCallback<Relationship> callback) {
//        if(isAuthorized(callback)){
//            Call<Relationship> getRelationshipCall = getClient().getRelationship(userId, creditCardId, deviceId);
//            getRelationshipCall.enqueue(new CallbackWrapper<>(callback));
//        }
//    }
//
//    /**
//     * Retrieves the details of an existing credit card.
//     * You need only supply the unique identifier that was returned upon creation.
//     *
//     * @param userId       user id
//     * @param creditCardId credit card id
//     * @param callback     result callback
//     */
//    public void getCreditCard(final String userId, final String creditCardId, final ApiCallback<CreditCard> callback) {
//        if (isAuthorized(callback)) {
//
//            Runnable onSuccess = new Runnable() {
//                @Override
//                public void run() {
//                    Call<CreditCard> getCreditCardCall = getClient().getCreditCard(userId, creditCardId);
//                    getCreditCardCall.enqueue(new CallbackWrapper<>(callback));
//                }
//            };
//
//            checkKeyAndMakeCall(onSuccess, callback);
//        }
//    }
//
//    /**
//     * Retrieves the details of an existing device.
//     * You need only supply the unique identifier that was returned upon creation.
//     *
//     * @param userId   user id
//     * @param deviceId device id
//     * @param callback result callback
//     */
//    public void getDevice(final String userId, final String deviceId, final ApiCallback<Device> callback) {
//        if(isAuthorized(callback)){
//
//            Runnable onSuccess = new Runnable() {
//                @Override
//                public void run() {
//
//                    Call<Device> getDeviceCall = getClient().getDevice(userId, deviceId);
//                    getDeviceCall.enqueue(new CallbackWrapper<>(callback));
//                }
//            };
//
//            checkKeyAndMakeCall(onSuccess, callback);
//        }
//    }
//
//    /**
//     * Retrieves an individual commit.
//     *
//     * @param userId   user id
//     * @param deviceId device id
//     * @param commitId commit id
//     * @param callback result callback
//     */
//    public void getCommit(final String userId, final String deviceId, final String commitId, final ApiCallback<Commit> callback) {
//        if(isAuthorized(callback)){
//
//            Runnable onSuccess = new Runnable() {
//                @Override
//                public void run() {
//
//                    Call<Commit> getCommitCall = getClient().getCommit(userId, deviceId, commitId);
//                    getCommitCall.enqueue(new CallbackWrapper<>(callback));
//                }
//            };
//
//            checkKeyAndMakeCall(onSuccess, callback);
//        }
//    }
//
//    /**
//     * Get a single transaction.
//     *
//     * @param userId        user id
//     * @param creditCardId credit card id
//     * @param transactionId transaction id
//     * @param callback      result callback
//     */
//    public void getTransaction(String userId, String creditCardId, String transactionId, ApiCallback<Transaction> callback) {
//        if(isAuthorized(callback)){
//            Call<Transaction> getTransactionCall = getClient().getTransaction(userId, creditCardId, transactionId);
//            getTransactionCall.enqueue(new CallbackWrapper<>(callback));
//        }
//    }
//

    private <T> void makeCall(final Call<JsonElement> call, final Type type,
                              final ApiCallback<T> callback) {
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

    public <T> void get(final String url, final Map<String, Object> queryMap, final Type type,
                        final ApiCallback<T> callback) {
        Call<JsonElement> getDataCall = queryMap != null ?
                getClient().get(url, queryMap) : getClient().get(url);
        makeCall(getDataCall, type, callback);
    }

    public <T, U> void post(final String url, final U data, final Type type,
                            final ApiCallback<T> callback) {
        Call<JsonElement> postDataCall = data != null ?
                getClient().post(url, data) : getClient().post(url);
        makeCall(postDataCall, type, callback);
    }

    public <U> void post(String url, final U data, final ApiCallback<Void> callback) {
        Call<Void> postDataCall = data != null ?
                getClient().postNoResponse(url, data) : getClient().postNoResponse(url);
        postDataCall.enqueue(new CallbackWrapper<>(callback));
    }

    public <T, U> void patch(final String url, final U data, final boolean add,
                             final boolean encrypt, final Type type, final ApiCallback<T> callback) {
        JsonArray updateData = new JsonArray();

        Map<String, Object> userMap = ObjectConverter.convertToSimpleMap(data);
        for (Map.Entry<String, Object> entry : userMap.entrySet()) {
            JsonObject item = new JsonObject();
            item.addProperty("op", add ? "add" : "replace");
            item.addProperty("path", entry.getKey());
            item.addProperty("value", String.valueOf(entry.getValue()));

            updateData.add(item);
        }

        Call<JsonElement> patchDataCall = null;

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

    public <T> void put(final String url, final T data, final Type type,
                        final ApiCallback<T> callback) {
        Call<JsonElement> putDataCall = getClient().put(url, data);
        makeCall(putDataCall, type, callback);
    }

    public void delete(String url, final ApiCallback<Void> callback) {
        Call<Void> deleteDataCall = getClient().delete(url);
        deleteDataCall.enqueue(new CallbackWrapper<>(callback));
    }

}
