package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminSalesResponse {

    private Integer totalSales;      // 총 매출
    private Integer totalOrders;     // 총 주문 수
    private Integer todaySales;      // 오늘 매출
    private Integer todayOrders;     // 오늘 주문 수

}