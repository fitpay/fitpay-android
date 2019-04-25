package com.fitpay.android.configs;

import androidx.annotation.NonNull;

import com.fitpay.android.utils.Constants;
import com.fitpay.android.utils.StringUtils;

/**
 * Fitpay config model. Internal class
 */
class FitpayConfigModel {
    private String clientId;
    private String webURL;
    private String redirectURL;
    private String apiURL;
    private String authURL;
    private boolean supportApp2App;
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
        return !StringUtils.isEmpty(webURL) ? webURL : Constants.CONFIG_WV_URL;
    }

    String getRedirectUrl() {
        return !StringUtils.isEmpty(redirectURL) ? redirectURL : Constants.CONFIG_REDIRECT_URL;
    }

    String getApiURL() {
        return !StringUtils.isEmpty(apiURL) ? apiURL : Constants.CONFIG_API_BASE_URL;
    }

    String getAuthURL() {
        return !StringUtils.isEmpty(authURL) ? authURL : Constants.CONFIG_AUTH_BASE_URL;
    }

    boolean isApp2AppSupported(){
        return supportApp2App;
    }

    FitpayConfigWebModel getWebConfig() {
        if(web == null){
            web = new FitpayConfigWebModel();
        }
        return web;
    }
}
