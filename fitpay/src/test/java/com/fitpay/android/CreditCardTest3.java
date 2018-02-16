package com.fitpay.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.fitpay.android.api.models.card.CreditCard;
import com.fitpay.android.api.models.collection.Collections;
import com.fitpay.android.api.models.device.Device;
import com.fitpay.android.paymentdevice.utils.CreditCardPreferenceData;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;


/**
 * Created by tgs on 4/21/16.
 */
public class CreditCardTest3 extends TestActions {

    private String creditCardId;
    private String sessionData;

    @Test
    public void testCanAcceptTerms() throws Exception {
        final Context context = Mockito.mock(Context.class);
        final SharedPreferences mockPrefs = Mockito.mock(SharedPreferences.class);
        final SharedPreferences.Editor mockEditor = Mockito.mock(SharedPreferences.Editor.class);

        when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(mockPrefs);
        when(mockPrefs.edit()).thenReturn(mockEditor);
        when(mockPrefs.getAll()).then(invocation -> {
            Map<String, String> map = new HashMap<>();
            map.put(creditCardId, sessionData);
            return map;
        });
        when(mockEditor.putString(anyString(), any())).thenAnswer(invocation -> {
            creditCardId = (String) invocation.getArguments()[0];
            sessionData = (String) invocation.getArguments()[1];
            return mockEditor;
        });
        when(mockEditor.remove(anyString())).thenReturn(mockEditor);
        when(mockEditor.commit()).thenReturn(true);

        Device device = getTestDevice();
        Device createdDevice = createDevice(user, device);
        assertNotNull("created device", createdDevice);

        Collections.DeviceCollection devices = getDevices(user);
        assertNotNull("devices collection should not be null", devices);
        assertEquals("should have one device", 1, devices.getTotalResults());

        String pan = "9999445454545454";
        CreditCard creditCard = getTestCreditCard(pan);

        creditCard = createCreditCard(user, creditCard);
        assertNotNull("card not created", creditCard);
        assertEquals("card not in expected state", "ELIGIBLE", creditCard.getState());

        //save acceptTerms sessionData
        CreditCardPreferenceData.store(context, creditCard);

        //get the same card but without sessionData
        creditCard = getCreditCard(creditCard);

        //update acceptTerms url
        CreditCardPreferenceData.update(context, creditCard.getUserId(), creditCard);

        //accept terms
        creditCard = acceptTerms(creditCard);

        //remove sessionData
        CreditCardPreferenceData.remove(context, creditCard);

        assertNotNull("card not successfully updated by accept terms", creditCard);
        assertEquals("card state", "PENDING_VERIFICATION", creditCard.getState());
    }
}

