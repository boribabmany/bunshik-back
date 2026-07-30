package com.bunshik.admin.jwt;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AdminJwtTokenProviderTest {

    private static final String SECRET =
            "admin-test-secret-key-must-be-at-least-32-bytes";

    @Test
    void createdTokenContainsAdminClaims() {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, 60_000);

        String token = tokenProvider.createToken(7, "manager");

        assertThat(tokenProvider.validateToken(token)).isTrue();
        assertThat(tokenProvider.getAdminId(token)).isEqualTo(7);
        assertThat(tokenProvider.getUsername(token)).isEqualTo("manager");
        assertThat(tokenProvider.getRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void malformedTokenIsInvalid() {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, 60_000);

        assertThat(tokenProvider.validateToken("not-a-jwt")).isFalse();
    }

    @Test
    void expiredTokenIsInvalid() {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, -1);

        String token = tokenProvider.createToken(7, "manager");

        assertThat(tokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void tokenSignedWithDifferentSecretIsInvalid() {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, 60_000);
        AdminJwtTokenProvider attackerTokenProvider =
                new AdminJwtTokenProvider(
                        "different-test-secret-key-must-be-at-least-32-bytes",
                        60_000
                );

        String forgedToken =
                attackerTokenProvider.createToken(7, "manager");

        assertThat(tokenProvider.validateToken(forgedToken)).isFalse();
    }
}
