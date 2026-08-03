package com.bunshik.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
public class SalesAnalyticsResponse {

    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private PeriodSalesSummaryResponse summary;
    private List<SalesHistoryResponse> history;
    private List<MenuSalesResponse> menuStats;
    private List<PaymentMethodSalesResponse> paymentStats;
}
