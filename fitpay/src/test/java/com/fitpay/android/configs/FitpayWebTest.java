package com.fitpay.android.configs;

import android.app.Activity;
import android.app.Application;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.fitpay.android.TestActions;
import com.fitpay.android.a2averification.A2AIssuerResponse;
import com.fitpay.android.a2averification.A2AVerificationFailed;
import com.fitpay.android.a2averification.A2AVerificationRequest;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NamedResource;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;
import com.fitpay.android.webview.models.IdVerification;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.schedulers.Schedulers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FitpayWebTest extends TestActions {

    @ClassRule
    public static NamedResource rule = new NamedResource(FitpayWebTest.class);

    private Activity activity;
    private WebViewCommunicatorImpl webViewCommunicator;
    private MockPaymentDeviceConnector deviceConnector;
    private FitpayWeb fitpayWeb;

    @Override
    @Before
    public void before() {
        activity = Mockito.mock(Activity.class);

        Application application = Mockito.mock(Application.class);
        Mockito.when(activity.getApplicationContext()).thenReturn(application);

        WebView view = Mockito.mock(WebView.class);
        WebSettings settings = Mockito.mock(WebSettings.class);
        Mockito.when(view.getSettings()).thenReturn(settings);

        deviceConnector = new MockPaymentDeviceConnector(activity);
        webViewCommunicator = new WebViewCommunicatorImpl(activity, deviceConnector, view);
        fitpayWeb = new FitpayWeb(activity, view, webViewCommunicator, deviceConnector);
    }

    @Override
    @After
    public void after() {
        super.after();
        fitpayWeb.onDestroy();
    }

    @Test
    public void test01_initFitpayView() {
        Assert.assertNotNull(fitpayWeb);
        Assert.assertEquals(webViewCommunicator, fitpayWeb.getCommunicator());
        Assert.assertNotNull(fitpayWeb.getWebClient());
        Assert.assertNotNull(fitpayWeb.getWebChromeClient(activity));
    }

    @Test
    public void test02_setupFitpayWeb() {

        String email = "test@test.test";
        boolean userHasAccount = true;
        String accessToken = "aabbcc";
        Device device = getTestDevice();

        fitpayWeb.setupWebView(email, userHasAccount, accessToken, device);

        String baseConfig = getConfig(email, userHasAccount, accessToken, device);
        String fpConfig = fitpayWeb.getConfig();

        Assert.assertEquals(baseConfig, fpConfig);
    }

    @Test
    public void test03_checkDelegates() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(3);

        FitpayConfig.supportApp2App = true;

        //rtm delegate
        final AtomicReference<String> rtmTypeRef = new AtomicReference<>();
        final FitpayWeb.RtmDelegate rtmDelegate = rtmMessage -> {
            FPLog.d(FitpayWebTest.class.getSimpleName(), "event received:" + rtmMessage.toString());
            rtmTypeRef.set(rtmMessage.getType());
            latch.countDown();
        };
        fitpayWeb.setRtmDelegate(rtmDelegate);

        //idVerification delegate
        final IdVerification idVerification = new IdVerification.Builder().build();
        final FitpayWeb.IdVerificationDelegate idVerificationDelegate = () -> idVerification;
        fitpayWeb.setIdVerificationDelegate(idVerificationDelegate);

        //a2a delegate
        final AtomicReference<A2AVerificationRequest> a2aRequest = new AtomicReference<>();
        final FitpayWeb.A2AVerificationDelegate a2AVerificationDelegate = new FitpayWeb.A2AVerificationDelegate() {
            @Override
            public void onRequestReceived(A2AVerificationRequest verificationRequest) {
                a2aRequest.set(verificationRequest);
                latch.countDown();
            }

            @Override
            public void processA2AIssuerResponse(A2AIssuerResponse successResponse) {

            }

            @Override
            public void onA2AVerificationFailed(A2AVerificationFailed failedResponse) {

            }
        };
        fitpayWeb.setA2AVerificationDelegate(a2AVerificationDelegate);

        AtomicReference<IdVerificationRequest> idRequestRef = new AtomicReference<>();
        IdVerificationRequestListener listener = new IdVerificationRequestListener(deviceConnector.id(), latch, idRequestRef);
        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());

        RtmMessage testMessage = new RtmMessage("1", "", "myEvent");
        RxBus.getInstance().post(deviceConnector.id(), testMessage);
        RxBus.getInstance().post(deviceConnector.id(), new IdVerificationRequest("1"));

        RxBus.getInstance().post(deviceConnector.id(), getA2AVerificationRequest());

        latch.await(30, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(listener);

        Assert.assertEquals("myEvent", rtmTypeRef.get());
        Assert.assertNotNull(idRequestRef.get());
        Assert.assertNotNull(a2aRequest.get());
    }

    private String getConfig(String email, boolean hasAccount, String token, Device device) {
        WvConfig config = new WvConfig.Builder()
                .email(email)
                .accountExist(hasAccount)
                .clientId(FitpayConfig.clientId)
                .setCSSUrl(FitpayConfig.Web.cssURL)
                .redirectUri(FitpayConfig.redirectURL)
                .setBaseLanguageUrl(FitpayConfig.Web.baseLanguageURL)
                .setAccessToken(token)
                .demoMode(FitpayConfig.Web.demoMode)
                .demoCardGroup(FitpayConfig.Web.demoCardGroup)
                .useWebCardScanner(!FitpayConfig.Web.supportCardScanner)
                .paymentDevice(new WvPaymentDeviceInfoSecure(device))
                .build();

        Assert.assertNotNull(config);

        return config.getEncodedString();
    }

    private class IdVerificationRequestListener extends Listener {
        IdVerificationRequestListener(String id, CountDownLatch latch, AtomicReference<IdVerificationRequest> ref) {
            super(id);
            mCommands.put(IdVerificationRequest.class, data -> {
                FPLog.d(data.toString());
                ref.set((IdVerificationRequest) data);
                latch.countDown();
            });
        }
    }
}
