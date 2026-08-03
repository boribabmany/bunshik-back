package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.dto.AdminLoginResponseDto;
import com.bunshik.admin.jwt.AdminJwtTokenProvider;
import com.bunshik.admin.mappers.AdminAuthMapper;
import com.bunshik.admin.security.LoginAttemptLimitException;
import com.bunshik.common.entity.AdminUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminAuthServiceTest {

    @Mock
    private AdminAuthMapper adminAuthMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminJwtTokenProvider adminJwtTokenProvider;

    @Mock
    private AdminLoginAttemptService loginAttemptService;

    @InjectMocks
    private AdminAuthService adminAuthService;

    @Test
    void loginReturnsAccessTokenForActiveAdmin() {
        AdminLoginRequestDto request = loginRequest("admin", "password123");
        AdminUser admin = adminUser(1, "admin", "encoded-password", true);

        when(adminAuthMapper.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("password123", "encoded-password"))
                .thenReturn(true);
        when(adminJwtTokenProvider.createToken(1, "admin"))
                .thenReturn("access-token");

        AdminLoginResponseDto response = adminAuthService.login(
                request,
                "127.0.0.1"
        );

        assertThat(response.getId()).isEqualTo(1);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        verify(adminJwtTokenProvider).createToken(1, "admin");
        verify(loginAttemptService).recordSuccess("admin", "127.0.0.1");
    }

    @Test
    void loginRejectsUnknownUsername() {
        AdminLoginRequestDto request = loginRequest("missing", "password123");
        when(adminAuthMapper.findByUsername("missing")).thenReturn(null);

        assertThatThrownBy(() -> adminAuthService.login(request, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");

        verifyNoInteractions(passwordEncoder, adminJwtTokenProvider);
    }

    @Test
    void loginRejectsInactiveAdmin() {
        AdminLoginRequestDto request = loginRequest("admin", "password123");
        AdminUser admin = adminUser(1, "admin", "encoded-password", false);
        when(adminAuthMapper.findByUsername("admin")).thenReturn(admin);

        assertThatThrownBy(() -> adminAuthService.login(request, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비활성화된 관리자 계정입니다.");

        verifyNoInteractions(passwordEncoder, adminJwtTokenProvider);
    }

    @Test
    void loginRejectsWrongPassword() {
        AdminLoginRequestDto request = loginRequest("admin", "wrong-password");
        AdminUser admin = adminUser(1, "admin", "encoded-password", true);

        when(adminAuthMapper.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);

        assertThatThrownBy(() -> adminAuthService.login(request, "127.0.0.1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 올바르지 않습니다.");

        verifyNoInteractions(adminJwtTokenProvider);
    }

    @Test
    void fifthFailedLoginReturnsRateLimitError() {
        AdminLoginRequestDto request = loginRequest("admin", "wrong-password");
        AdminUser admin = adminUser(1, "admin", "encoded-password", true);

        when(adminAuthMapper.findByUsername("admin")).thenReturn(admin);
        when(passwordEncoder.matches("wrong-password", "encoded-password"))
                .thenReturn(false);
        when(loginAttemptService.recordFailure("admin", "127.0.0.1"))
                .thenReturn(true);

        assertThatThrownBy(() -> adminAuthService.login(request, "127.0.0.1"))
                .isInstanceOf(LoginAttemptLimitException.class)
                .hasMessage("로그인에 5회 실패하여 10분간 로그인이 제한됩니다.");

        verifyNoInteractions(adminJwtTokenProvider);
    }

    private AdminLoginRequestDto loginRequest(String username, String password) {
        AdminLoginRequestDto request = new AdminLoginRequestDto();
        request.setUsername(username);
        request.setPassword(password);
        return request;
    }

    private AdminUser adminUser(
            Integer id,
            String username,
            String passwordHash,
            boolean active
    ) {
        AdminUser admin = new AdminUser();
        admin.setId(id);
        admin.setUsername(username);
        admin.setPasswordHash(passwordHash);
        admin.setIsActive(active);
        return admin;
    }
}
