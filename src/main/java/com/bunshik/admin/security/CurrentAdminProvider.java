package com.bunshik.admin.security;

import com.bunshik.admin.jwt.AdminPrincipal;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentAdminProvider {

    public Integer getAdminId() {
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !(authentication.getPrincipal()
                instanceof AdminPrincipal principal)) {
            throw new AuthenticationCredentialsNotFoundException(
                    "관리자 인증 정보가 없습니다."
            );
        }

        return principal.adminId();
    }
}