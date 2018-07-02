package com.fitpay.android.webview.impl;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.fitpay.android.R;
import com.fitpay.android.a2averification.A2AVerificationFailed;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.api.ApiManager;
import com.fitpay.android.api.callbacks.ApiCallback;
import com.fitpay.android.api.enums.ResultCode;
import com.fitpay.android.api.enums.SyncInitiator;
import com.fitpay.android.api.models.UserStreamEvent;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.api.models.security.OAuthToken;
import com.fitpay.android.api.models.user.User;
import com.fitpay.android.api.sse.UserEventStreamListener;
import com.fitpay.android.api.sse.UserEventStreamManager;
import com.fitpay.android.cardscanner.IFitPayCardScanner;
import com.fitpay.android.cardscanner.ScannedCardInfo;
import com.fitpay.android.configs.FitpayConfig;
import com.fitpay.android.paymentdevice.DeviceService;
import com.fitpay.android.paymentdevice.constants.States;
import com.fitpay.android.paymentdevice.enums.Sync;
import com.fitpay.android.paymentdevice.events.NotificationSyncRequest;
import com.fitpay.android.paymentdevice.interfaces.PaymentDeviceConnectable;
import com.fitpay.android.paymentdevice.models.SyncInfo;
import com.fitpay.android.paymentdevice.models.SyncRequest;
import com.fitpay.android.utils.EventCallback;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.webview.WebViewCommunicator;
import com.fitpay.android.webview.enums.RtmType;
import com.fitpay.android.webview.events.DeviceStatusMessage;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.events.RtmMessageResponse;
import com.fitpay.android.webview.events.UserReceived;
import com.fitpay.android.webview.models.IdVerification;
import com.fitpay.android.webview.models.RtmVersion;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

import static com.fitpay.android.utils.Constants.WV_DATA;

/**
 * Created by Ross Gabay on 4/13/2016.
 * Implementation of the WebViewCommunicator interface
 */
public class WebViewCommunicatorImpl implements WebViewCommunicator {

    private final String TAG = WebViewCommunicatorImpl.class.getSimpleName();

    private static final int RESPONSE_OK = 0;
    private static final int RESPONSE_FAILURE = 1;
    private static final int RESPONSE_IN_PROGRESS = 2;

    private final Activity activity;
    private DeviceService deviceService;
    private final PaymentDeviceConnectable deviceConnector;

    private User user;
    private Device device;
    private String deviceId = null;

    private DeviceStatusListener deviceStatusListener;

    private DeviceSyncListener listenerForAppCallbacks;
    private DeviceSyncListener listenerForAppCallbacksNoCallbackId;
    private UserEventStreamListener userEventStreamSyncListener;
    private RtmMessageListener rtmMessageListener;

    private PushNotificationSyncListener pushNotificationSyncListener;

    private IdVerificationListener idVerificationListener;
    private A2AListener a2AListener;

    private WebView webView;

    private RtmVersion webAppRtmVersion = new RtmVersion(RtmType.RTM_VERSION);

    private final Gson gson = new Gson();

    private IFitPayCardScanner cardScanner;

    private boolean supportsAppVerification;

    public WebViewCommunicatorImpl(Activity ctx, PaymentDeviceConnectable deviceConnector, WebView webView) {
        this.activity = ctx;
        this.deviceConnector = deviceConnector;

        NotificationManager.getInstance().addListener(deviceStatusListener = new DeviceStatusListener());
        NotificationManager.getInstance().addListener(rtmMessageListener = new RtmMessageListener());
        NotificationManager.getInstance().addListener(pushNotificationSyncListener = new PushNotificationSyncListener());
        NotificationManager.getInstance().addListener(idVerificationListener = new IdVerificationListener());
        NotificationManager.getInstance().addListener(a2AListener = new A2AListener());

        this.webView = webView;
    }

    /**
     * set custom card scanner instead of Jumio
     *
     * @param cardScanner custom card scanner
     */
    public void setCardScanner(IFitPayCardScanner cardScanner) {
        this.cardScanner = cardScanner;
    }

    public IFitPayCardScanner getCardScanner() {
        return this.cardScanner;
    }

    /**
     * this method should be called manually in {@link Activity#onDestroy()}
     */
    public void close() {
        NotificationManager.getInstance().removeListener(deviceStatusListener);
        NotificationManager.getInstance().removeListener(rtmMessageListener);
        NotificationManager.getInstance().removeListener(pushNotificationSyncListener);
        NotificationManager.getInstance().removeListener(listenerForAppCallbacks);
        NotificationManager.getInstance().removeListener(listenerForAppCallbacksNoCallbackId);
        NotificationManager.getInstance().removeListener(idVerificationListener);
        NotificationManager.getInstance().removeListener(a2AListener);
        NotificationManager.getInstance().removeListener(userEventStreamSyncListener);

        if (user != null) {
            UserEventStreamManager.unsubscribe(user.getId());
        }
    }

    /**
     * send logout message to JS
     */
    public void logout() {
        postMessage(new RtmMessageResponse("logout"));
        postMessage(new DeviceStatusMessage(activity.getString(R.string.fp_connecting), deviceId, DeviceStatusMessage.PENDING));
    }

    /**
     * call this function in {@link Activity#onBackPressed()}
     */
    public void onBack() {
        postMessage(new RtmMessageResponse("back"));
    }

    /**
     * response for a {@link #onBack()} function.
     */
    public void onNoHistory() {
        activity.finish();
    }

    /**
     * call this function asap to retrieve webapp version of RTM
     */
    public void sendRtmVersion() {
        postMessage(new RtmMessageResponse(new RtmVersion(RtmType.RTM_VERSION), RtmType.VERSION));
    }

    public void sendCardData(String callbackId, @NonNull ScannedCardInfo cardInfo) {
        postMessage(new RtmMessageResponse(callbackId, cardInfo, RtmType.CARD_SCANNED));
    }

    @Override
    @JavascriptInterface
    public void dispatchMessage(String message) throws JSONException {
        if (message == null) {
            FPLog.w(WV_DATA, "\\Received\\: invalid message");
            throw new IllegalArgumentException("invalid message");
        }

        JSONObject obj = new JSONObject(message);

        String callBackId = obj.getString("callBackId");
        if (callBackId == null) {
            FPLog.w(WV_DATA, "\\Received\\: callBackId is missing in the message");
            throw new IllegalArgumentException("callBackId is missing in the message");
        }

        String type = obj.getString("type");
        if (type == null) {
            FPLog.w(WV_DATA, "\\Received\\: type is missing in the message");
            throw new IllegalArgumentException("type is missing in the message");
        }

        FPLog.i(WV_DATA, String.format(Locale.getDefault(), "\\Received\\: callbackId:%s type:%s", callBackId, type));

        String dataStr = obj.has("data") ? obj.getString("data") : null;

        postMessage(new RtmMessage(callBackId, dataStr, type));
    }

    @Override
    @JavascriptInterface
    public void sync(String callbackId) {
        sync(callbackId, null);
    }

    @Override
    public void sync(String callbackId, final SyncInfo syncInfo) {
        FPLog.d(TAG, "sync received");

        if (null == user) {
            onTaskError(EventCallback.SYNC_COMPLETED, callbackId, "No user specified for sync operation");
            return;
        }

        if (null == device) {
            onTaskError(EventCallback.SYNC_COMPLETED, callbackId, "No device specified for sync operation");
            return;
        }

        if (null == deviceConnector) {
            onTaskError(EventCallback.SYNC_COMPLETED, callbackId, "No PaymentConnector has not been configured for sync operation");
            return;
        }

        NotificationManager.getInstance().removeListener(listenerForAppCallbacks);
        NotificationManager.getInstance().removeListener(listenerForAppCallbacksNoCallbackId);

        NotificationManager.getInstance().addListener(listenerForAppCallbacks = new DeviceSyncListener(callbackId));

        createSyncRequest(syncInfo);
    }

    @Override
    @JavascriptInterface
    public void sendUserData(String callbackId, String deviceId, String token, String userId) {
        this.deviceId = deviceId;

        FPLog.d(TAG, "sendUserData received data: deviceId: " + deviceId + ", token: " + token + ", userId: " + userId);

        OAuthToken oAuthToken = new OAuthToken.Builder()
                .accessToken(token)
                .build();

        ApiManager.getInstance().setAuthToken(oAuthToken);

        getUserAndDevice(deviceId, callbackId);
    }

    //not used by the first iteration of the webview
    @Override
    @JavascriptInterface
    public String retrieveConfigJson() {
        throw new UnsupportedOperationException("method not supported in this iteration");
    }

    private void sendMessageToJs(String callBackId, boolean success, Object response) {
        postMessage(new RtmMessageResponse(callBackId, success, response, RtmType.RESOLVE));
    }

    private void sendDeviceStatusToJs(DeviceStatusMessage event) {
        postMessage(new RtmMessageResponse(event, RtmType.DEVICE_STATUS));
    }

    private void getUserAndDevice(final String deviceId, final String callbackId) {
        ApiManager.getInstance().getUser(new ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (result == null) {
                    onTaskError(EventCallback.USER_CREATED, callbackId, "getUser failed: result is null");
                    return;
                }

                WebViewCommunicatorImpl.this.user = result;

                postMessage(new UserReceived(user.getId(), user.getUsername()));

                EventCallback eventCallback = new EventCallback.Builder()
                        .setCommand(EventCallback.USER_CREATED)
                        .setStatus(EventCallback.STATUS_OK)
                        .build();
                eventCallback.send();

                result.getDevice(deviceId, new ApiCallback<Device>() {
                    @Override
                    public void onSuccess(Device result) {
                        WebViewCommunicatorImpl.this.device = result;

                        boolean automaticallySubscribeToUserEventStream = FitpayConfig.Web.automaticallySubscribeToUserEventStream;
                        if (automaticallySubscribeToUserEventStream) {

                            UserEventStreamManager.subscribe(user.getId());

                            boolean automaticSyncThroughUserEventStream = FitpayConfig.Web.automaticallySyncFromUserEventStream;
                            if (automaticSyncThroughUserEventStream) {
                                userEventStreamSyncListener = new UserEventStreamListener() {
                                    @Override
                                    public void onUserEvent(UserStreamEvent event) {
                                        if ("SYNC".equals(event.getType())) {
                                            SyncInfo syncInfo = gson.fromJson(event.getPayload(), SyncInfo.class);
                                            syncInfo.setInitiator(SyncInitiator.PLATFORM);

                                            createSyncRequest(syncInfo);
                                        }
                                    }
                                };

                                NotificationManager.getInstance().addListener(userEventStreamSyncListener);
                            }

                        }

                        String token = FitpayConfig.User.pushNotificationToken;
                        String deviceToken = device.getNotificationToken();

                        final Runnable onSuccess = () -> onTaskSuccess(EventCallback.GET_USER_AND_DEVICE, callbackId);

                        if (deviceToken == null || !deviceToken.equals(token)) {
                            Device updatedDevice = new Device.Builder().setNotificationToken(token).build();
                            device.updateToken(updatedDevice, deviceToken == null, new ApiCallback<Device>() {
                                @Override
                                public void onSuccess(Device result) {
                                    WebViewCommunicatorImpl.this.device = result;

                                    onSuccess.run();
                                }

                                @Override
                                public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                                    onTaskError(EventCallback.GET_USER_AND_DEVICE, callbackId, "update device failed:" + errorMessage);
                                }
                            });
                        } else {
                            onSuccess.run();
                        }
                    }

                    @Override
                    public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                        onTaskError(EventCallback.GET_USER_AND_DEVICE, callbackId, "getDevice failed " + errorMessage);
                    }
                });
            }

            @Override
            public void onFailure(@ResultCode.Code int errorCode, String errorMessage) {
                onTaskError(EventCallback.USER_CREATED, callbackId, "getUser failed " + errorMessage);
            }
        });
    }

    private void onTaskSuccess(@EventCallback.Command String command, String callbackId) {
        onTaskSuccess(command, callbackId, RESPONSE_OK);
    }

    private void onTaskSuccess(@EventCallback.Command String command, String callbackId, int response) {
        AppResponseModel stubResponse = new AppResponseModel.Builder()
                .status(response)
                .build();

        if (null != callbackId) {
            sendMessageToJs(callbackId, true, stubResponse);
        }

        EventCallback eventCallback = new EventCallback.Builder()
                .setCommand(command)
                .setStatus(EventCallback.STATUS_OK)
                .build();
        eventCallback.send();
    }

    private void onTaskError(@EventCallback.Command String command, String callbackId, String errorMessage) {
        AppResponseModel failedResponse = new AppResponseModel.Builder()
                .status(RESPONSE_FAILURE)
                .reason(errorMessage)
                .build();

        FPLog.w(TAG, errorMessage);

        if (null != callbackId) {
            sendMessageToJs(callbackId, false, gson.toJson(failedResponse));
        }

        postMessage(new DeviceStatusMessage(activity.getString(R.string.fp_sync_failed, errorMessage), deviceId, DeviceStatusMessage.ERROR));

        EventCallback eventCallback = new EventCallback.Builder()
                .setCommand(command)
                .setStatus(EventCallback.STATUS_FAILED)
                .setReason(errorMessage)
                .build();
        eventCallback.send();
    }

    public String getCurrentDeviceId() {
        return deviceId;
    }

    public String getConnectorId() {
        return deviceConnector != null ? deviceConnector.id() : null;
    }

    private void createSyncRequest(SyncInfo syncInfo) {
        if (deviceConnector != null) {
            new SyncRequest.Builder()
                    .setSyncInfo(syncInfo)
                    .setConnector(deviceConnector)
                    .setUser(user)
                    .setDevice(device)
                    .build()
                    .send();
        } else {
            Log.e(TAG, "Can't create syncRequest. PaymentDeviceConnector is missing");
        }
    }

    public void setWebAppRtmVersion(RtmVersion version) {
        webAppRtmVersion = version;
    }

    @Override
    public void startScan(String callbackId) {
        if (cardScanner != null) {
            FPLog.d("cardScan requested");
            cardScanner.startScan(callbackId);
        } else {
            FPLog.e("cardScan requested, however there is no cardScanner implementation provided");
        }
    }

    @Override
    public IdVerification getIdVerification() {
        return new IdVerification.Builder().build();
    }

    @Override
    public boolean supportsAppVerification() {
        return supportsAppVerification;
    }

    public void setSupportsAppVerification(boolean supportsAppVerification) {
        this.supportsAppVerification = supportsAppVerification;
    }

    /**
     * Get app-to-app return location
     * <p>
     * On completion of the issuer intent the OEM app must then open the web-view using the returnLocation.
     * <baseUrl>/<returnLocation>?config=<base64 encoded config with a2a>
     *
     * @return a2a return location
     */
    public String getA2aReturnLocation() {
        return a2AListener != null ? a2AListener.returnLocation : null;
    }

    public void postMessage(Object object) {
        RxBus.getInstance().post(getConnectorId(), object);
    }


    /**
     * Listen to device status
     */
    private class DeviceStatusListener extends Listener {
        private DeviceStatusListener() {
            super(getConnectorId());
            mCommands.put(DeviceStatusMessage.class, data -> sendDeviceStatusToJs((DeviceStatusMessage) data));
        }
    }

    /**
     * Listen to sync status
     */
    private class DeviceSyncListener extends Listener {

        private String callbackId;

        private DeviceSyncListener(String callbackId) {
            super(getConnectorId());
            this.callbackId = callbackId;
            mCommands.put(Sync.class, data -> onSyncStateChanged((Sync) data));

            if (callbackId != null) {
                FPLog.d("new DeviceSyncListener for callbackId (" + callbackId + ")");
            }
        }

        private void onSyncStateChanged(Sync syncEvent) {
            if (callbackId != null) {
                FPLog.d(TAG, "received on sync state changed event, callbackId (" + callbackId + "): " + syncEvent);
            }

            switch (syncEvent.getState()) {
                case States.COMPLETED:
                case States.COMPLETED_NO_UPDATES: {
                    if (callbackId != null) {
                        onTaskSuccess(EventCallback.SYNC_COMPLETED, callbackId);
                    }
                    postMessage(new DeviceStatusMessage(activity.getString(R.string.fp_sync_finished), deviceId, DeviceStatusMessage.SUCCESS));

                    NotificationManager.getInstance().removeListener(listenerForAppCallbacks);
                    NotificationManager.getInstance().removeListener(listenerForAppCallbacksNoCallbackId);

                    NotificationManager.getInstance().addListener(listenerForAppCallbacksNoCallbackId = new DeviceSyncListener(null));
                    break;
                }
                case States.TIMEOUT:
                case States.FAILED: {
                    onTaskError(EventCallback.SYNC_COMPLETED, callbackId, !StringUtils.isEmpty(syncEvent.getMessage()) ? syncEvent.getMessage() : "sync failure");

                    NotificationManager.getInstance().removeListener(listenerForAppCallbacks);
                    NotificationManager.getInstance().removeListener(listenerForAppCallbacksNoCallbackId);

                    NotificationManager.getInstance().addListener(listenerForAppCallbacksNoCallbackId = new DeviceSyncListener(null));
                    break;
                }
                default: {
                    FPLog.d(TAG, "skipping sync changed event: " + syncEvent);
                    break;
                }
            }
        }
    }

    /**
     * Listen to {@link RtmMessage} and {@link RtmMessageResponse}
     */
    private class RtmMessageListener extends Listener {
        private RtmMessageListener() {
            super(getConnectorId());
            mCommands.put(RtmMessage.class, data -> {
                RtmMessage msg = (RtmMessage) data;
                try {
                    RtmParserImpl.parse(WebViewCommunicatorImpl.this, webAppRtmVersion.getVersion(), msg);
                } catch (Exception e) {
                    onTaskError(EventCallback.DATA_PARSED, msg.getCallbackId(), e.getMessage());
                }
            });
            mCommands.put(RtmMessageResponse.class, data -> {
                String str = data.toString();
                FPLog.i(WV_DATA, "\\Response\\: " + str);
                final String url = "javascript:window.RtmBridge.resolve('" + str + "');";
                activity.runOnUiThread(() -> webView.loadUrl(url));
            });
        }
    }

    /**
     * Listen to push {@link NotificationSyncRequest}
     */
    private class PushNotificationSyncListener extends Listener {
        private PushNotificationSyncListener() {
            mCommands.put(NotificationSyncRequest.class, data -> sync(null, ((NotificationSyncRequest) data).getSyncInfo()));
        }
    }

    /**
     * Listen to {@link IdVerificationRequest}
     */
    private class IdVerificationListener extends Listener {
        private IdVerificationListener() {
            super(getConnectorId());
            mCommands.put(IdVerificationRequest.class, data ->
                    getIdVerification().send(getConnectorId(), ((IdVerificationRequest) data).getCallbackId()));
        }
    }

    /**
     * Listen to a2a request {@link A2AVerificationRequest} and {@link A2AVerificationFailed}
     */
    private class A2AListener extends Listener {
        private String requestCallbackId;
        private String returnLocation;

        private A2AListener() {
            super(getConnectorId());
            mCommands.put(A2AVerificationRequest.class, data -> {
                returnLocation = ((A2AVerificationRequest) data).getReturnLocation();
                requestCallbackId = ((A2AVerificationRequest) data).getCallbackId();
            });
            mCommands.put(A2AVerificationFailed.class, data ->
                    postMessage(new RtmMessageResponse(requestCallbackId, false, data, RtmType.APP_TO_APP_VERIFICATION)));
        }
    }
}


