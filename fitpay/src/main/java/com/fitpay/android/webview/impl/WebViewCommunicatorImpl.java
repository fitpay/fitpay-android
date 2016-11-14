package com.fitpay.android.webview.impl;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.fitpay.android.R;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.models.apdu.ApduExecutionResult;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.paymentdevice.DeviceService;
import com.fitpay.android.paymentdevice.callbacks.IListeners;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.CommitFailed;
import com.fitpay.android.paymentdevice.events.CommitSkipped;
import com.fitpay.android.paymentdevice.events.CommitSuccess;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.WebViewCommunicator;
import com.fitpay.android.webview.callback.OnTaskCompleted;
import com.fitpay.android.webview.events.DeviceStatusMessage;
import com.fitpay.android.webview.events.UserReceived;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Ross Gabay on 4/13/2016.
 * Implementation of the WebViewCommunicator interface
 */
public class WebViewCommunicatorImpl implements WebViewCommunicator {

    private final String TAG = WebViewCommunicatorImpl.class.getSimpleName();

    private static String USER_DATA_STUB_RESPONSE = "0";
    private static String SYNC_STUB_RESPONSE = "0";
    private static final String RESPONSE_FAILURE = "1";
    private static final String APP_CALLBACK_STATUS_OK = "OK";
    private static final String APP_CALLBACK_STATUS_FAILED = "FAILED";

    private final Activity activity;
    private OnTaskCompleted callback;
    private DeviceService deviceService;

    private User user;
    private Device device;

    private DeviceStatusListener deviceStatusListener;
    private CustomListener listenerForAppCallbacks;

    private WebView webView;

    private final Gson gson = new Gson();

    public WebViewCommunicatorImpl(Activity ctx, int wId, OnTaskCompleted callback) {
        this(ctx, wId);
        this.callback = callback;
    }

    public WebViewCommunicatorImpl(Activity ctx, int wId) {
        this.activity = ctx;
        deviceStatusListener = new DeviceStatusListener();
        NotificationManager.getInstance().addListener(deviceStatusListener);

        webView = (WebView) activity.findViewById(wId);
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Override
    public void logout() {
        sendLogoutSignalToJs();

        RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.connecting), DeviceStatusMessage.PENDING));
    }

    @Override
    @JavascriptInterface
    public void dispatchMessage(String message) throws JSONException {

        if (message == null) throw new IllegalArgumentException("invalid message");

        Log.d(TAG, "received message: " + message);
        JSONObject obj = new JSONObject(message);

        String callBackId = obj.getString("callBackId");
        if (callBackId == null)
            throw new IllegalArgumentException("action is missing in the message from the UI");
        Log.d(TAG, "received callbackId: " + callBackId);

        String action = obj.getJSONObject("data").getString("action");
        if (action == null)
            throw new IllegalArgumentException("action is missing in the message from the UI");

        switch (action) {

            case "userData":
                //params are only there for the userData action
                String deviceId = null;
                String token = null;
                String userId = null;

                try {
                    deviceId = obj.getJSONObject("data").getJSONObject("data").getString("deviceId");
                    token = obj.getJSONObject("data").getJSONObject("data").getString("token");
                    userId = obj.getJSONObject("data").getJSONObject("data").getString("userId");
                } catch (Exception e) {
                    throw new IllegalArgumentException("missing required message data");
                }

                sendUserData(callBackId, deviceId, token, userId);
                break;

            case "sync":
                sync(callBackId);
                break;

            default:
                throw new IllegalArgumentException("unsupported action value in message");

        }
    }

    @Override
    @JavascriptInterface
    public String sync(String callbackId) {

        RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_started), DeviceStatusMessage.PROGRESS));

        if (null == device) {
            SyncResponseModel response = new SyncResponseModel.Builder()
                    .status(RESPONSE_FAILURE)
                    .reason("No device specified for sync operation")
                    .build();

            Log.d(TAG, "sync can not be done.  No device has been specified.   response: " + response);

            if (null != callbackId) {
                sendMessageToJs(callbackId, "false", gson.toJson(response));
            }
            if (null != callback) {
                callback.onTaskCompleted(RESPONSE_FAILURE);
            }

            RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_failed), DeviceStatusMessage.ERROR));

            return gson.toJson(response);
        }

        if (null == deviceService) {
            SyncResponseModel response = new SyncResponseModel.Builder()
                    .status(RESPONSE_FAILURE)
                    .reason("No DeviceService has not been configured for sync operation")
                    .build();

            Log.d(TAG, "sync can not be done.  No device service configured.   response: " + response);

            if (null != callbackId) {
                sendMessageToJs(callbackId, "false", gson.toJson(response));
            }
            if (null != callback) {
                callback.onTaskCompleted(RESPONSE_FAILURE);
            }

            RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_failed), DeviceStatusMessage.ERROR));

            return gson.toJson(response);
        }

        NotificationManager.getInstance().removeListener(listenerForAppCallbacks);

        listenerForAppCallbacks = new CustomListener(callbackId);
        NotificationManager.getInstance().addListener(listenerForAppCallbacks);

        try {
            deviceService.syncData(user, device);
        } catch (IllegalStateException ex) {
            SyncResponseModel response = new SyncResponseModel.Builder()
                    .status(RESPONSE_FAILURE)
                    .reason(ex.getMessage())
                    .build();

            Log.d(TAG, "sync can not be done.  Reason: " + ex.getMessage() + ",  response: " + response);

            if (null != callbackId) {
                sendMessageToJs(callbackId, "true", gson.toJson(response));
            }
            if (null != callback) {
                callback.onTaskCompleted(RESPONSE_FAILURE);
            }

            RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_failed), DeviceStatusMessage.ERROR));
        }

        AckResponseModel stubResponse = new AckResponseModel.Builder()
                .status(USER_DATA_STUB_RESPONSE)
                .build();

        Log.d(TAG, "sync providing synchronous ack response: " + stubResponse);
        return gson.toJson(stubResponse);
    }

    @Override
    @JavascriptInterface
    public String sendUserData(String callbackId, String deviceId, String token, String userId) {

        Log.d(TAG, "sendUserData received data: deviceId: " + deviceId + ", token: " + token + ", userId: " + userId);

        OAuthToken oAuthToken = new OAuthToken.Builder()
                .accessToken(token)
                .userId(userId)
                .build();

        ApiManager.getInstance().setAuthToken(oAuthToken);

        // Get user and device asynchronously

        Log.d(TAG, "sendUserData initiating asynchronous retrieval of user and device");
        getUserAndDevice(deviceId, callbackId);

        // provide synchronous ack

        AckResponseModel stubResponse = new AckResponseModel.Builder()
                .status(USER_DATA_STUB_RESPONSE)
                .build();

        Log.d(TAG, "sendUserData providing synchronous ack response: " + stubResponse);
        return gson.toJson(stubResponse);
    }

    //not used by the first iteration of the webview
    @Override
    @JavascriptInterface
    public String retrieveConfigJson() {
        throw new UnsupportedOperationException("method not supported in this iteration");
    }

    @Override
    public void close() {
        NotificationManager.getInstance().removeListener(deviceStatusListener);
        NotificationManager.getInstance().removeListener(listenerForAppCallbacks);
    }

    public void sendMessageToJs(String callBackId, String success, String response) {

        String responseMessage = "{ \"callBackId\" :" + callBackId + "," +
                "\"success\" :" + success + "," +
                "\"response\" :" + response + " }";

        Log.d(TAG, "sending message to webview: " + responseMessage);

        final String url = "javascript:window.RtmBridge.resolve('" + responseMessage + "');";
        Log.d(TAG, "message url: " + url);

        activity.runOnUiThread(() -> webView.loadUrl(url));
    }

    public void sendDeviceStatusToJs(DeviceStatusMessage event) {

        String responseMessage = "{\"message\":\"" + event.getMessage() + "\",\"type\":" + event.getType() + "}";

        final String url = "javascript:window.RtmBridge.setDeviceStatus(" + responseMessage + ");";
        Log.d(TAG, "message url: " + url);

        activity.runOnUiThread(() -> webView.loadUrl(url));
    }

    public void sendLogoutSignalToJs() {
        Log.d(TAG, "sending logout message to the webview");

        final String jurl = "javascript:window.RtmBridge.forceLogout();";

        activity.runOnUiThread(() -> webView.loadUrl(jurl));
    }

    private void getUserAndDevice(final String deviceId, final String callbackId) {
        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                WebViewCommunicatorImpl.this.user = result;

                RxBus.getInstance().post(new UserReceived(user.getUsername()));

                result.getDevice(deviceId, new ApiCallback<Device>() {
                    @Override
                    public void onSuccess(Device result) {
                        WebViewCommunicatorImpl.this.device = result;

                        String token = ApiManager.getPushToken();
                        String deviceToken = device.getNotificationToken();

                        if (deviceToken == null || !deviceToken.equals(token)) {
                            Device updatedDevice = new Device.Builder().setNotificationToken(token).build();
                            device.updateToken(updatedDevice, deviceToken == null, new ApiCallback<Device>() {
                                @Override
                                public void onSuccess(Device result) {
                                    WebViewCommunicatorImpl.this.device = result;
                                }

                                @Override
                                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                                    Log.e(TAG, errorMessage);
                                }
                            });
                        }

                        AckResponseModel stubResponse = new AckResponseModel.Builder()
                                .status(USER_DATA_STUB_RESPONSE)
                                .build();
                        if (null != callbackId) {
                            sendMessageToJs(callbackId, "true", gson.toJson(stubResponse));
                        }
                        if (null != callback) {
                            callback.onTaskCompleted(USER_DATA_STUB_RESPONSE);
                        }
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        Log.d(TAG, "getDevice failed " + errorMessage);
                        //TODO handle failure and report back to WVC
                    }
                });

            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                Log.d(TAG, "getUser failed " + errorMessage);
                //TODO handle failure and report back to WVC
            }
        });

    }

    private class DeviceStatusListener extends Listener {
        private DeviceStatusListener() {
            super();
            mCommands.put(DeviceStatusMessage.class, data -> sendDeviceStatusToJs((DeviceStatusMessage) data));
        }
    }

    private class CustomListener extends Listener implements IListeners.ApduListener, IListeners.SyncListener {

        private String callbackId;

        private CustomListener(String callbackId) {
            super();
            this.callbackId = callbackId;
            mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));
            mCommands.put(CommitSuccess.class, data -> onCommitSuccess((CommitSuccess) data));
            mCommands.put(CommitFailed.class, data -> onCommitFailed((CommitFailed) data));
        }

        @Override
        public void onApduPackageResultReceived(ApduExecutionResult result) {

        }

        @Override
        public void onApduPackageErrorReceived(ApduExecutionResult result) {

        }

        @Override
        public void onSyncStateChanged(Sync syncEvent) {
            Log.d(TAG, "received on sync state changed event: " + syncEvent);
            switch (syncEvent.getState()) {
                case States.COMPLETED:
                case States.COMPLETED_NO_UPDATES: {
                    AckResponseModel stubResponse = new AckResponseModel.Builder()
                            .status(USER_DATA_STUB_RESPONSE)
                            .build();

                    if (null != callbackId) {
                        sendMessageToJs(callbackId, "true", gson.toJson(stubResponse));
                    }
                    if (null != callback) {
                        callback.onTaskCompleted(USER_DATA_STUB_RESPONSE);
                    }

                    RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_finished), DeviceStatusMessage.SUCCESS));

                    break;
                }
                case States.FAILED: {
                    SyncResponseModel response = new SyncResponseModel.Builder()
                            .status(RESPONSE_FAILURE)
                            .reason("sync failure")
                            .build();
                    if (null != callbackId) {
                        sendMessageToJs(callbackId, "false", gson.toJson(response));
                    }
                    if (null != callback) {
                        callback.onTaskCompleted(RESPONSE_FAILURE);
                    }

                    RxBus.getInstance().post(new DeviceStatusMessage(activity.getString(R.string.sync_failed), DeviceStatusMessage.ERROR));
                    break;
                }
                default: {
                    Log.d(TAG, "skipping sync changed event: " + syncEvent);
                    break;
                }
            }
        }

        @Override
        public void onCommitFailed(CommitFailed commitFailed) {
            Log.d(TAG, "received commit failed event: " + commitFailed.getCommitId());

            if (callback != null)
                callback.onTaskCompleted(buildAppCallbackPayload(
                        commitFailed.getCommitType(),
                        APP_CALLBACK_STATUS_FAILED,
                        commitFailed.getErrorMessage(),
                        commitFailed.getCreatedTs()));

        }

        @Override
        public void onCommitSuccess(CommitSuccess commitSuccess) {
            Log.d(TAG, "Successful commit reported, type: " + commitSuccess.getCommitType() + ", id: " + commitSuccess.getCommitId());

            if (callback != null)
                callback.onTaskCompleted(buildAppCallbackPayload(
                        commitSuccess.getCommitType(),
                        APP_CALLBACK_STATUS_OK,
                        "",
                        commitSuccess.getCreatedTs()));

        }

        @Override
        public void onCommitSkipped(CommitSkipped commitSkipped) {
            Log.d(TAG, "Skipped commit reported, type: " + commitSkipped.getCommitType() + ", id: " + commitSkipped.getCommitId());

            callback.onTaskCompleted(buildAppCallbackPayload(
                    commitSkipped.getCommitType(),
                    APP_CALLBACK_STATUS_OK,
                    "",
                    commitSkipped.getCreatedTs()));

        }

    }

    private String buildAppCallbackPayload(String command, String status, String reason, long createdTs) {

        AppCallbackModel appCallbackPayload = new AppCallbackModel();

        Date date = new Date(createdTs);
        Format format = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss.SSS");

        appCallbackPayload.setTimestamp(format.format(date));
        appCallbackPayload.setCommand(command);
        appCallbackPayload.setStatus(status);
        appCallbackPayload.setReason(reason);

        return new Gson().toJson(appCallbackPayload);
    }
}


