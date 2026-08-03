package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.dto.AdminLoginResponseDto;
import com.bunshik.admin.service.AdminAuthService;
import com.bunshik.common.ApiResponse;

import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminAuthController {


    private final AdminAuthService adminAuthService;


    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AdminLoginResponseDto>> login(
            @Valid @RequestBody AdminLoginRequestDto dto,
            HttpServletRequest request) {

        AdminLoginResponseDto response = adminAuthService.login(
                dto,
                request.getRemoteAddr()
        );

        return ResponseEntity.ok(
                ApiResponse.success(response, "로그인 성공")
        );
    }
}

