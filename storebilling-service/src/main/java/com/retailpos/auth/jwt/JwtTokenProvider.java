package com.retailpos.auth.jwt;

import com.retailpos.auth.userdetails.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Issues and parses JWTs. Spring Security itself only *consumes* an established
 * {@code Authentication} - it does not issue tokens, hence jjwt here.
 *
 * <p>Claims embedded: {@code sub} (username), {@code userId}, {@code roles} (role codes),
 * {@code organizationId}, {@code storeAccess} (store ids, or {@code allStores: true}), plus the
 * standard {@code exp}/{@code iat}/{@code jti}. Permissions are deliberately NOT embedded -
 * authorization is always re-resolved server-side per request from role -&gt; permission via
 * {@code CustomUserDetailsService}, to avoid staleness.
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties jwtProperties;
    private SecretKey signingKey;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    void init() {
        String secret = jwtProperties.getSecret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException(
                    "JWT_SECRET environment variable is not set. Refusing to start with no signing key.");
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET must be at least 32 bytes (256 bits) for HS256. Provide a longer secret.");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserPrincipal principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(jwtProperties.getExpirationMs());

        List<Long> storeAccess = principal.isAllStoreAccess() ? List.of() : List.copyOf(principal.getStoreIds());

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getId())
                .claim("roles", principal.getRoleCodes())
                .claim("organizationId", principal.getOrganizationId())
                .claim("allStores", principal.isAllStoreAccess())
                .claim("storeAccess", storeAccess)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey)
                .compact();
    }

    public long getExpirationMs() {
        return jwtProperties.getExpirationMs();
    }

    /** Parses and signature/expiry-validates a token, throwing {@link JwtException} if invalid. */
    public Jws<Claims> parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public String getUsername(String token) {
        return parseClaims(token).getPayload().getSubject();
    }
}
