package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.dto.AdminLoginResponseDto;
import com.bunshik.admin.service.AdminAuthService;
import com.bunshik.common.entity.AdminUser;
import jakarta.validation.Valid;
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
    public ResponseEntity<AdminLoginResponseDto> login(
            @Valid @RequestBody AdminLoginRequestDto dto) {

        return ResponseEntity.ok(adminAuthService.login(dto));
    }

}

