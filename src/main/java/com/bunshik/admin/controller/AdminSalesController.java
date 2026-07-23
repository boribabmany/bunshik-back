package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminSalesSummaryResponse;
import com.bunshik.admin.dto.PopularMenuResponse;
import com.bunshik.admin.dto.SalesHistoryResponse;
import com.bunshik.admin.service.AdminSalesService;
import com.bunshik.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sales")
@RequiredArgsConstructor
public class AdminSalesController {

    private final AdminSalesService adminSalesService;


    // 매출 요약 카드 데이터
    @GetMapping("/summary")
    public ApiResponse<AdminSalesSummaryResponse> getSalesSummary() {

        return ApiResponse.success(
                adminSalesService.getSalesSummary()
        );
    }

    // 인기 메뉴 TOP5
    @GetMapping("/popular")
    public ApiResponse<List<PopularMenuResponse>> getPopularMenus() {

        return ApiResponse.success(
                adminSalesService.getPopularMenus()
        );
    }
    // 최근 매출 내역 (그래프/테이블)
    @GetMapping("/history")
    public ApiResponse<List<SalesHistoryResponse>> getSalesHistory() {

        return ApiResponse.success(
                adminSalesService.getSalesHistory()
        );
    }
}