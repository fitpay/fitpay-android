package com.fitpay.android.cardscanner;

import com.fitpay.android.configs.FitpayConfig;

/**
 * RTM response data for {@value com.fitpay.android.webview.enums.RtmType#SUPPORT_CARD_SCANNER}
 */
public class CardScannerResponse {

    private boolean supportCardScanner;

    public CardScannerResponse() {
        supportCardScanner = FitpayConfig.Web.isCardScannerSupported();
    }
}
