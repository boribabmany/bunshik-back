package com.bunshik.admin.dto;

import lombok.Data;

@Data
public class AdminSalesSummaryResponse {

    private int todaySales;

    private int todayOrders;

    private int monthlySales;

    private int yesterdaySales;

    private double averageOrderPrice;

    private int completedOrders;

}