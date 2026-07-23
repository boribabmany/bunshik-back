package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminLoginRequestDto;
import com.bunshik.admin.mappers.AdminAuthMapper;
import com.bunshik.common.entity.AdminUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminAuthMapper adminAuthMapper;


    public AdminUser login(AdminLoginRequestDto dto){

        return adminAuthMapper.findByUsername(dto.getUsername());

    }
}