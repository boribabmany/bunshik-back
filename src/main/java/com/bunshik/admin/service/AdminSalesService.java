package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminSalesSummaryResponse;
import com.bunshik.admin.dto.PopularMenuResponse;
import com.bunshik.admin.dto.SalesHistoryResponse;
import com.bunshik.admin.mappers.AdminSalesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminSalesService {

    private final AdminSalesMapper adminSalesMapper;

    public AdminSalesSummaryResponse getSalesSummary() {
        return adminSalesMapper.getSalesSummary();
    }

    public List<PopularMenuResponse> getPopularMenus() {
        return adminSalesMapper.getPopularMenus();
    }

    public List<SalesHistoryResponse> getSalesHistory() {
        return adminSalesMapper.getSalesHistory();
    }
}