package com.gpse.basis.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("security")
public final class SecurityConstants {

    private String authLoginUrl;

    // Signing key für den HS512-Algorithm
    // Eigene Schlüssel könnt ihr unter http://www.allkeysgenerator.com/ erstellen lassen.
    private String jwtSecret;

    // JWT Token-Standardwerte
    private String tokenHeader;
    private String tokenPrefix; //<5>
    private String tokenType; //<6>
    private String tokenIssuer; //<7>
    private String tokenAudience; //<8>

    public String getAuthLoginUrl() {
        return authLoginUrl;
    }

    public void setAuthLoginUrl(final String authLoginUrl) {
        this.authLoginUrl = authLoginUrl;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(final String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public String getTokenHeader() {
        return tokenHeader;
    }

    public void setTokenHeader(final String tokenHeader) {
        this.tokenHeader = tokenHeader;
    }

    public String getTokenPrefix() {
        return tokenPrefix;
    }

    public void setTokenPrefix(final String tokenPrefix) {
        this.tokenPrefix = tokenPrefix;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(final String tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenIssuer() {
        return tokenIssuer;
    }

    public void setTokenIssuer(final String tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    public String getTokenAudience() {
        return tokenAudience;
    }

    public void setTokenAudience(final String tokenAudience) {
        this.tokenAudience = tokenAudience;
    }
}
