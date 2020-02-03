package com.fitpay.android.utils;

import java.io.IOException;

public class ExpiredTokenException extends IOException {

    private static final long serialVersionUID = 1L;

    public ExpiredTokenException(String message) {
        super(message);
    }

}
