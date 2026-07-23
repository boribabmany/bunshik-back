package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.service.AdminAuthService;
import com.bunshik.common.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminAuthController {


    private final AdminAuthService adminAuthService;


    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody AdminLoginRequestDto dto){

        AdminUser admin =
                adminAuthService.login(dto);

        return ResponseEntity.ok(admin);
    }

}