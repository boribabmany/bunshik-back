package com.bunshik.admin.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SalesHistoryResponse {

    private LocalDate salesDate;
    private int orderCount;
    private int totalSales;
}