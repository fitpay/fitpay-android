package com.fitpay.android.utils;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JsonHelper<T> {

    public Call<T> getCall(String fileName){
        return new Call<T>() {
            @Override
            public Response<T> execute() throws IOException {
                return null;
            }

            @Override
            public void enqueue(Callback<T> callback) {

            }

            @Override
            public boolean isExecuted() {
                return true;
            }

            @Override
            public void cancel() {

            }

            @Override
            public boolean isCanceled() {
                return false;
            }

            @Override
            public Call<T> clone() {
                return null;
            }

            @Override
            public Request request() {
                return null;
            }
        };
    }

    public static Object readObject(String file, Class clazz) {
        java.net.URL url = clazz.getResource("test/resources/json/" + file + ".json");
        java.nio.file.Path resPath = null;
        try {
            resPath = java.nio.file.Paths.get(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String text = null;
        try {
            text = new String(java.nio.file.Files.readAllBytes(resPath), "UTF8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Constants.getGson().fromJson(text, clazz);
    }
}
