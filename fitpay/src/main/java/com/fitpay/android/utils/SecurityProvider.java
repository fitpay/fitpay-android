package com.fitpay.android.utils;

import android.support.annotation.NonNull;

import org.conscrypt.Conscrypt;

import java.security.Provider;
import java.security.Security;

/**
 * Singleton that store custom Java security API provider
 */

public class SecurityProvider {

    private static final String TAG = SecurityProvider.class.getName();
    private static SecurityProvider sInstance;

    /**
     * Retrieve security provider instance
     *
     * @return security provider singleton
     */
    public static SecurityProvider getInstance() {
        if (sInstance == null) {
            sInstance = new SecurityProvider();
        }
        return sInstance;
    }

    private Provider provider;

    private SecurityProvider() {
    }

    /**
     * Get custom provider
     *
     * @return Java security API provider
     */
    public Provider getProvider() {
        return provider;
    }

    /**
     * Set custom provider
     *
     * @param provider Java security API provider
     */
    public void setProvider(@NonNull Provider provider) {
        this.provider = provider;
    }

    /**
     * Init custom provider
     */
    void initProvider() {
        try {
            if (provider == null && Conscrypt.isAvailable()) {
                provider = Conscrypt.newProvider();
            }
            if (provider != null) {
                Security.removeProvider(provider.getName());
                Security.insertProviderAt(provider, 1);
            }
        } catch (Exception e) {
            FPLog.e(TAG, e);
        }
    }
}
