package com.fitpay.android.configs;

/**
 * Fitpay web config model. Internal class
 */
class FitpayConfigWebModel {
    boolean demoMode = false;
    String demoCardGroup;
    String cssURL;
    String language;
    String baseLanguageURL;
    boolean supportCardScanner = false;
    boolean automaticallySubscribeToUserEventStream = true;
    boolean automaticallySyncFromUserEventStream = true;
}
