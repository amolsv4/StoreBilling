package com.retailpos.auth.jwt;

import com.retailpos.auth.userdetails.UserPrincipal;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        // Random per-run test-only secret - never a literal/reused value.
        properties.setSecret(UUID.randomUUID() + "" + UUID.randomUUID());
        properties.setExpirationMs(1_800_000L);
        properties.setIssuer("storebilling-test");
        jwtTokenProvider = new JwtTokenProvider(properties);
        jwtTokenProvider.init();
    }

    @Test
    @SuppressWarnings("unchecked") // Claims.get(String, Class) can't express List<String>/List<Number> - type
                                    // erasure means this is JJWT's own API shape, not something we control.
    void generateAndParseRoundTripsAllExpectedClaims() {
        UserPrincipal principal = principal(42L, "jdoe", 7L, false, Set.of(100L, 200L),
                List.of("ROLE_STORE_MANAGER", "STORE_VIEW"), List.of("STORE_MANAGER"));

        String token = jwtTokenProvider.generateToken(principal);
        Claims claims = jwtTokenProvider.parseClaims(token).getPayload();

        assertThat(claims.getSubject()).isEqualTo("jdoe");
        assertThat(claims.get("userId", Long.class)).isEqualTo(42L);
        assertThat(claims.get("organizationId", Long.class)).isEqualTo(7L);
        assertThat(claims.get("allStores", Boolean.class)).isFalse();
        List<String> roles = claims.get("roles", List.class);
        assertThat(roles).containsExactly("STORE_MANAGER");
        List<Number> storeAccess = claims.get("storeAccess", List.class);
        assertThat(storeAccess).extracting(Number::intValue).containsExactlyInAnyOrder(100, 200);
        assertThat(claims.getId()).isNotBlank();
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void validateTokenReturnsTrueForFreshlyIssuedToken() {
        String token = jwtTokenProvider.generateToken(principal(1L, "alice", null, true, Set.of(), List.of(), List.of()));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUsername(token)).isEqualTo("alice");
    }

    @Test
    void validateTokenReturnsFalseForTamperedSignature() {
        String token = jwtTokenProvider.generateToken(principal(1L, "alice", null, true, Set.of(), List.of(), List.of()));
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("A") ? "B" : "A") + "x";

        assertThat(jwtTokenProvider.validateToken(tampered)).isFalse();
    }

    @Test
    void validateTokenReturnsFalseForExpiredToken() throws InterruptedException {
        JwtProperties shortLived = new JwtProperties();
        shortLived.setSecret(UUID.randomUUID() + "" + UUID.randomUUID());
        shortLived.setExpirationMs(1L);
        shortLived.setIssuer("storebilling-test");
        JwtTokenProvider shortLivedProvider = new JwtTokenProvider(shortLived);
        shortLivedProvider.init();

        String token = shortLivedProvider.generateToken(principal(1L, "alice", null, true, Set.of(), List.of(), List.of()));
        Thread.sleep(25);

        assertThat(shortLivedProvider.validateToken(token)).isFalse();
    }

    @Test
    void initThrowsWhenSecretIsBlank() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("");
        JwtTokenProvider provider = new JwtTokenProvider(properties);

        assertThatIllegalStateExceptionIsThrown(provider);
    }

    private void assertThatIllegalStateExceptionIsThrown(JwtTokenProvider provider) {
        try {
            provider.init();
            org.junit.jupiter.api.Assertions.fail("Expected IllegalStateException for blank secret");
        } catch (IllegalStateException expected) {
            assertThat(expected.getMessage()).contains("JWT_SECRET");
        }
    }

    private UserPrincipal principal(Long id, String username, Long organizationId, boolean allStoreAccess,
                                     Set<Long> storeIds, List<String> extraAuthorities, List<String> roleCodes) {
        Set<GrantedAuthority> authorities = new java.util.HashSet<>();
        for (String authority : extraAuthorities) {
            authorities.add(new SimpleGrantedAuthority(authority));
        }
        return new UserPrincipal(id, username, "hashed-password", true, false,
                organizationId, allStoreAccess, storeIds, authorities, roleCodes);
    }
}
