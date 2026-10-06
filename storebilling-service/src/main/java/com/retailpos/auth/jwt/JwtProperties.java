package com.retailpos.auth.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the {@code jwt.*} configuration keys. The secret comes ONLY from the {@code JWT_SECRET}
 * environment variable (see application.yml: {@code jwt.secret: ${JWT_SECRET}}) - there is no
 * fallback default here, intentionally, so a missing env var fails fast at startup rather than
 * silently using a weak/predictable secret.
 */
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** Must be at least 256 bits (32 bytes) of secret material for HS256. */
    private String secret;

    private long expirationMs = 1_800_000L;

    private String issuer = "storebilling";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public void setExpirationMs(long expirationMs) {
        this.expirationMs = expirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
