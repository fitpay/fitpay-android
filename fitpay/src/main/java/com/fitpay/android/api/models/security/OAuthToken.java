package com.fitpay.android.api.models.security;

import com.fitpay.android.utils.FPLog;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.util.Calendar;
import java.util.Date;

/**
 * OAuth token.
 */
final public class OAuthToken {
    private final String tokenType;
    private final String accessToken;
    private final long expiresIn;
    private final Date expiresTs;
    private final Date issuedTs;
    private final String userId;

    private OAuthToken(String tokenType, String accessToken, long expiresIn, Date expiresTs, Date issuedTs, String userId) {
        this.tokenType = tokenType;
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.expiresTs = expiresTs;
        this.issuedTs = issuedTs;
        this.userId = userId;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public Date getExpiresTs() {
        return expiresTs;
    }

    public Date getIssuedTs() {
        return issuedTs;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isExpired() {
        // used the expired date/time contained in the bearer token if set
        if (expiresTs != null && expiresTs.before(new Date())) {
            return expiresTs.getTime() < System.currentTimeMillis();
        }

        // if expired date/time is not in the bearer token (it's optional), then use
        // the expires_in value from the authentication request itself
        if (expiresIn != -1 && issuedTs != null) {
            return (issuedTs.getTime() + (expiresIn*1000)) < System.currentTimeMillis();
        }

        // if we get here, then we don't have the information necessary to determine if a token
        // is expired or not
        return false;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("OAuthToken{");
        sb.append("tokenType='").append(tokenType).append('\'');
        sb.append(", accessToken='").append(accessToken).append('\'');
        sb.append(", expiresIn=").append(expiresIn);
        sb.append(", expiresTs=").append(expiresTs);
        sb.append(", issuedTs=").append(issuedTs);
        sb.append(", userId='").append(userId).append('\'');
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private String tokenType;
        private String accessToken;
        private long expiresIn = -1;
        private String userId = null;
        private Date expiresTs = null;
        private Date issuedTs = new Date();

        public Builder tokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        public Builder accessToken(String accessToken) {
            this.accessToken = accessToken;
            decodeAccessToken(this.accessToken);

            return this;
        }

        public Builder expiresIn(long expiresIn) {
            this.expiresIn = expiresIn;

            Calendar c = Calendar.getInstance();
            c.add(Calendar.SECOND, (int)expiresIn);
            this.expiresTs = c.getTime();

            return this;
        }

        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public Builder expiredTs(Date expiredTs) {
            this.expiresTs = expiredTs;
            return this;
        }

        public OAuthToken build() {
            OAuthToken token = new OAuthToken(tokenType, accessToken, expiresIn, expiresTs, issuedTs, userId);
            FPLog.d("oauth token: " + token);

            return token;
        }

        private void decodeAccessToken(String accessToken) {
            try {
                SignedJWT jwt = SignedJWT.parse(accessToken);

                JWTClaimsSet claims = jwt.getJWTClaimsSet();

                if (claims.getStringClaim("user_id") != null) {
                    this.userId = claims.getStringClaim("user_id");
                }

                if (claims.getExpirationTime() != null) {
                    this.expiresTs = claims.getExpirationTime();
                }

                if (claims.getIssueTime() != null) {
                    this.issuedTs = claims.getIssueTime();
                }
            } catch (Exception e) {
                FPLog.e(e);
            }
        }
    }
}
