package com.fitpay.android.utils;

import org.junit.rules.ExternalResource;

public class NamedResource extends ExternalResource {
    final String name;

    public NamedResource(Class clazz) {
        this.name = clazz.getSimpleName();
    }

    @Override
    protected void before() {
        HttpLogging.setTestName(name);
    }
}
