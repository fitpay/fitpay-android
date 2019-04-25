package com.fitpay.android.api.models.user;

import androidx.annotation.NonNull;

import com.fitpay.android.utils.StringUtils;
import com.fitpay.android.utils.ValidationException;

import java.util.Map;

/**
 * Login required data
 */
public final class LoginIdentity {

    private String username;
    private String password;

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public static class Builder {
        private String username = null;
        private String password = null;

        /**
         * Creates a Builder instance that can be used to build {@link Map<>} with various configuration
         * settings. Builder follows the builder pattern, and it is typically used by first
         * invoking various configuration methods to set desired options, and finally calling
         * {@link #build()}.
         */
        public Builder(){
        }

        /**
         * Creates a {@link LoginIdentity} instance based on the current configuration. This method is free of
         * side-effects to this {@code Builder} instance and hence can be called multiple times.
         *
         * @return an instance of {@link LoginIdentity} configured with the options currently set in this builder
         */
        public LoginIdentity build() throws ValidationException{
            LoginIdentity loginIdentity = new LoginIdentity();

            if(StringUtils.isEmpty(username)){
                throw new ValidationException("Username can't be null");
            }

            if(StringUtils.isEmpty(password)){
                throw new ValidationException("Password can't be null");
            }

            loginIdentity.username = username;
            loginIdentity.password = password;

            return loginIdentity;
        }

        /**
         * Set username
         * @param username username
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setUsername(@NonNull String username) {
            this.username = username;
            return this;
        }

        /**
         * Set password
         * @param password password
         * @return a reference to this {@code Builder} object to fulfill the "Builder" pattern
         */
        public Builder setPassword(@NonNull String password) {
            this.password = password;
            return this;
        }
    }
}
