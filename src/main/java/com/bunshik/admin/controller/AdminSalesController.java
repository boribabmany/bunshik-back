package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminSalesResponse;
import com.bunshik.admin.service.AdminSalesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final AdminSalesService adminSalesService;

    @GetMapping
    public AdminSalesResponse getSales() {

        return adminSalesService.getSalesSummary();

    }
}