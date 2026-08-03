package com.bunshik.admin.dto;

import lombok.Data;

@Data
public class PeriodSalesSummaryResponse {

    private long totalSales;
    private int orderCount;
    private double averageOrderPrice;
}
