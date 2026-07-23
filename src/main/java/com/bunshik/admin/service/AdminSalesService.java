package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminSalesResponse;
import com.bunshik.admin.mappers.AdminSalesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminSalesService {

    private final AdminSalesMapper adminSalesMapper;

    public AdminSalesResponse getSalesSummary() {
        return adminSalesMapper.getSalesSummary();
    }
}