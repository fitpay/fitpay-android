package com.fitpay.android.configs;

import android.app.Activity;
import android.app.Application;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.fitpay.android.TestActions;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.impl.mock.MockPaymentDeviceConnector;
import com.fitpay.android.utils.FPLog;
import com.fitpay.android.utils.Listener;
import com.fitpay.android.utils.NotificationManager;
import com.fitpay.android.utils.RxBus;
import com.fitpay.android.webview.events.IdVerificationRequest;
import com.fitpay.android.webview.events.RtmMessage;
import com.fitpay.android.webview.impl.WebViewCommunicatorImpl;
import com.fitpay.android.webview.models.IdVerification;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import rx.schedulers.Schedulers;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FitpayWebTest extends TestActions {

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
    public void after() throws Exception {
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

    //TODO: current test doesn't want to pass on travis, but it works on local machine. fix it later
    @Ignore
    @Test
    public void test03_checkDelegates() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(2);
        final AtomicReference<String> rtmTypeRef = new AtomicReference<>();
        final FitpayWeb.RtmDelegate rtmDelegate = rtmMessage -> {
            FPLog.d(FitpayWebTest.class.getSimpleName(), "event received:" + rtmMessage.toString());
            rtmTypeRef.set(rtmMessage.getType());
            latch.countDown();
        };
        fitpayWeb.setRtmDelegate(rtmDelegate);

        Assert.assertNotNull(fitpayWeb.getRtmDelegate());
        Assert.assertEquals(rtmDelegate, fitpayWeb.getRtmDelegate());

        final IdVerification idVerification = new IdVerification.Builder().build();
        final FitpayWeb.IdVerificationDelegate idVerificationDelegate = () -> idVerification;
        fitpayWeb.setIdVerificationDelegate(idVerificationDelegate);

        Assert.assertNotNull(fitpayWeb.getIdVerificationDelegate());
        Assert.assertEquals(idVerificationDelegate, fitpayWeb.getIdVerificationDelegate());

        AtomicReference<IdVerificationRequest> idRequestRef = new AtomicReference<>();
        IdVerificationRequestListener listener = new IdVerificationRequestListener(deviceConnector.id(), latch, idRequestRef);
        NotificationManager.getInstance().addListener(listener, Schedulers.immediate());

        RtmMessage testMessage = new RtmMessage("1", "", "myEvent");
        RxBus.getInstance().post(deviceConnector.id(), testMessage);
        RxBus.getInstance().post(deviceConnector.id(), new IdVerificationRequest("1"));

        latch.await(30, TimeUnit.SECONDS);

        NotificationManager.getInstance().removeListener(listener);

        Assert.assertEquals("myEvent", rtmTypeRef.get());
        Assert.assertNotNull(idRequestRef.get());
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
