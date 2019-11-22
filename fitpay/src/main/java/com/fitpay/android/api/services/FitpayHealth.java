package com.fitpay.android.api.services;

import com.fitpay.android.api.callbacks.ApiCallbackExt;
import com.fitpay.android.api.enums.ApiStatus;
import com.fitpay.android.api.models.ErrorResponse;
import com.fitpay.android.configs.FitpayConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FitpayHealth {
    public static void getApiStatus(ApiCallbackExt<ApiStatus> callback) {
        final Gson gson = new Gson();
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(FitpayConfig.apiURL + "/health")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String rspBody;
                try {
                    rspBody = response.body().string();
                } catch (IOException e) {
                    onFailure(call, e);
                    return;
                }

                ApiStatus status = null;
                if (!response.isSuccessful()) {
                    switch (response.code()) {
                        case 503:
                            if ("true".equals(response.header("Maintenance-Mode", response.header("maintenance-mode")))) {
                                status = ApiStatus.MAINTENANCE;
                                break;
                            } // else fallthrough
                        default:
                            callback.onFailure(response.code(), response.message());
                            return;
                    }
                } else {
                    try {
                        Map<String, Object> json = gson.fromJson(rspBody, Map.class);
                        status = gson.fromJson(((String) json.get("status")), ApiStatus.class);
                    } catch (Exception e) {
                        status = ApiStatus.UNAVAILABLE;
                    }
                }

                // default to UNAVAILABLE if there was an unrecognized status
                if (status == null) {
                    status = ApiStatus.UNAVAILABLE;
                }
                callback.onSuccess(status);
            }

            @Override
            public void onFailure(Call call, IOException e) {
                callback.onSuccess(ApiStatus.UNAVAILABLE);
            }
        });
    }

    public static void getMaintenanceMode(ApiCallbackExt<Boolean> callback) {
        getApiStatus(new ApiCallbackExt<ApiStatus>() {
            @Override
            public void onSuccess(ApiStatus result) {
                if (ApiStatus.MAINTENANCE.equals(result)) {
                    callback.onSuccess(true);
                } else {
                    callback.onSuccess(false);
                }
            }
            @Override
            public void onFailure(ErrorResponse apiErrorResponse) {
                callback.onFailure(apiErrorResponse);
            }
        });
    }
}
