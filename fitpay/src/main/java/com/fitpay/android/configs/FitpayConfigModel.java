package com.fitpay.android.configs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.StringUtils;

/**
 * Fitpay config model. Internal class
 */
class FitpayConfigModel {
    private String clientId;
    private String webUrl;
    private String redirectUrl;
    private String apiURL;
    private String authURL;
    private FitpayConfigWebModel web;

    FitpayConfigModel(@NonNull String clientId) {
        this.clientId = clientId;
    }

    String getClientId() {
        if(StringUtils.isEmpty(clientId)){
            throw new IllegalArgumentException("clientId can't be null");
        }
        return clientId;
    }

    String getWebUrl() {
        return !StringUtils.isEmpty(webUrl) ? webUrl : Constants.CONFIG_WV_URL;
    }

    String getRedirectUrl() {
        return !StringUtils.isEmpty(redirectUrl) ? redirectUrl : Constants.CONFIG_REDIRECT_URL;
    }

    String getApiURL() {
        return !StringUtils.isEmpty(apiURL) ? apiURL : Constants.CONFIG_API_BASE_URL;
    }

    String getAuthURL() {
        return !StringUtils.isEmpty(authURL) ? authURL : Constants.CONFIG_AUTH_BASE_URL;
    }

    FitpayConfigWebModel getWebConfig() {
        if(web == null){
            web = new FitpayConfigWebModel();
        }
        return web;
    }
}
