package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.dto.AdminLoginResponseDto;
import com.bunshik.admin.jwt.AdminJwtTokenProvider;
import com.bunshik.admin.mappers.AdminAuthMapper;
import com.bunshik.admin.security.LoginAttemptLimitException;
import com.bunshik.common.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminAuthMapper adminAuthMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtTokenProvider adminJwtTokenProvider;
    private final AdminLoginAttemptService loginAttemptService;

    public AdminLoginResponseDto login(
            AdminLoginRequestDto dto,
            String clientIp
    ) {
        loginAttemptService.ensureLoginAllowed(dto.getUsername(), clientIp);

        AdminUser admin =
                adminAuthMapper.findByUsername(dto.getUsername());

        if (admin == null) {
            recordFailure(dto.getUsername(), clientIp);
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        if (!Boolean.TRUE.equals(admin.getIsActive())) {
            recordFailure(dto.getUsername(), clientIp);
            throw new IllegalArgumentException(
                    "비활성화된 관리자 계정입니다."
            );
        }

        if (!passwordEncoder.matches(
                dto.getPassword(),
                admin.getPasswordHash()
        )) {
            recordFailure(dto.getUsername(), clientIp);
            throw new IllegalArgumentException(
                    "아이디 또는 비밀번호가 올바르지 않습니다."
            );
        }

        loginAttemptService.recordSuccess(dto.getUsername(), clientIp);

        String accessToken =
                adminJwtTokenProvider.createToken(
                        admin.getId(),
                        admin.getUsername()
                );

        return new AdminLoginResponseDto(
                admin.getId(),
                admin.getUsername(),
                accessToken,
                "Bearer"
        );
    }

    private void recordFailure(String username, String clientIp) {
        if (loginAttemptService.recordFailure(username, clientIp)) {
            throw new LoginAttemptLimitException();
        }
    }
}
