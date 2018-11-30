package com.fitpay.android.api;

import com.fitpay.android.Steps;
import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.models.Country;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.api.models.Province;
import com.fitpay.android.api.models.collection.CountryCollection;
import com.fitpay.android.api.models.collection.ProvincesCollection;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CountriesTest {

    private static Steps steps = null;

    @BeforeClass
    public static void init() {
        steps = new Steps(CountriesTest.class);
    }

    @Test
    public void test1_getCountries() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);

        CountryCollection[] countryCollections = new CountryCollection[1];
        ApiManager.getInstance().getCountries(new ApiCallbackExt<CountryCollection>() {
            @Override
            public void onFailure(ErrorResponse apiErrorResponse) {
                latch.countDown();
            }

            @Override
            public void onSuccess(CountryCollection result) {
                countryCollections[0] = result;
                latch.countDown();
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        Assert.assertNotNull("no result", countryCollections[0]);
        Assert.assertNotNull("result is empty", countryCollections[0].getCollection());

        Country country = countryCollections[0].getCountry("US");
        Assert.assertNotNull("country US is null", country);

        final CountDownLatch latch2 = new CountDownLatch(1);

        ProvincesCollection[] provincesCollections = new ProvincesCollection[1];
        country.getProvinces(new ApiCallbackExt<ProvincesCollection>() {
            @Override
            public void onFailure(ErrorResponse apiErrorResponse) {
                latch2.countDown();
            }

            @Override
            public void onSuccess(ProvincesCollection result) {
                provincesCollections[0] = result;
                latch2.countDown();
            }
        });

        latch2.await(10, TimeUnit.SECONDS);

        Assert.assertNotNull("no result", provincesCollections[0]);
        Assert.assertNotNull("result is empty", provincesCollections[0].getProvinces());

        Province province = provincesCollections[0].getProvince("AZ");
        Assert.assertNotNull("province AZ is null", province);
        Assert.assertEquals("province name is wrong", province.getName(), "Arizona");
    }
}
