package com.fitpay.android.configs;

/**
 * Fitpay web config model. Internal class
 */
class FitpayConfigWebModel {
    boolean demoMode = false;
    String demoCardGroup;
    String cssURL;
    String baseLanguageURL;
    @Deprecated
    boolean supportCardScanner = false;
    boolean automaticallySubscribeToUserEventStream = true;
    boolean automaticallySyncFromUserEventStream = true;
}
