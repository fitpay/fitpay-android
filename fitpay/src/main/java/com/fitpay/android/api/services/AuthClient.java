package com.fitpay.android.api.services;

import com.fitpay.android.api.models.security.OAuthToken;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface AuthClient {

    /**
     * Login user and get auth token
     */
    @FormUrlEncoded
    @POST("oauth/authorize")
    Call<OAuthToken> loginCredentials(@FieldMap Map<String, String> options);

    /**
     * Login user and get auth token
     */
    @FormUrlEncoded
    @POST("oauth/token")
    Call<OAuthToken> loginToken(@FieldMap Map<String, String> options);

}