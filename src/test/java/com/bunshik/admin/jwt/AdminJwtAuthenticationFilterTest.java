package com.bunshik.admin.jwt;

import com.bunshik.admin.security.RestAuthenticationEntryPoint;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminJwtAuthenticationFilterTest {

    private static final String SECRET =
            "admin-test-secret-key-must-be-at-least-32-bytes";

    @Mock
    private RestAuthenticationEntryPoint authenticationEntryPoint;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validTokenAuthenticatesAdminAndContinuesFilterChain()
            throws Exception {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, 60_000);
        AdminJwtAuthenticationFilter filter =
                new AdminJwtAuthenticationFilter(
                        tokenProvider,
                        authenticationEntryPoint
                );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain =
                (req, res) -> chainCalled.set(true);

        request.addHeader(
                "Authorization",
                "Bearer " + tokenProvider.createToken(7, "manager")
        );

        filter.doFilter(request, response, filterChain);

        assertThat(chainCalled).isTrue();
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNotNull();
        assertThat(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getPrincipal()
        )
                .isEqualTo(new AdminPrincipal(7, "manager"));
        assertThat(
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getAuthorities()
        )
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
        verify(authenticationEntryPoint, never())
                .commence(any(), any(), any());
    }

    @Test
    void invalidTokenReturnsUnauthorizedWithoutContinuingFilterChain()
            throws Exception {
        AdminJwtTokenProvider tokenProvider =
                new AdminJwtTokenProvider(SECRET, 60_000);
        AdminJwtAuthenticationFilter filter =
                new AdminJwtAuthenticationFilter(
                        tokenProvider,
                        authenticationEntryPoint
                );
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain filterChain =
                (req, res) -> chainCalled.set(true);

        request.addHeader("Authorization", "Bearer forged-token");

        filter.doFilter(request, response, filterChain);

        assertThat(chainCalled).isFalse();
        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isNull();
        verify(authenticationEntryPoint)
                .commence(any(), any(), any());
    }
}
