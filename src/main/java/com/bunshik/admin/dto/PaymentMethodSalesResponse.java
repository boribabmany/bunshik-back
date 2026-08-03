package com.bunshik.admin.dto;

import lombok.Data;

@Data
public class PaymentMethodSalesResponse {

    private String paymentMethod;
    private int orderCount;
    private long totalSales;
}
