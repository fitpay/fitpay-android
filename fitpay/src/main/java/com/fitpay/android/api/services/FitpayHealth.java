package com.fitpay.android.api.services;

import com.fitpay.android.configs.FitpayConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FitpayHealth {
    private final Gson gson = new Gson();

    public void getApiStatus() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(FitpayConfig.apiURL + "/health").build();
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                Map<String, Object> json = gson.fromJson(response.body().charStream(), Map.class);
                System.out.println(json);
            } else {

            }
        } catch (IOException e) {

        }

    }
}
