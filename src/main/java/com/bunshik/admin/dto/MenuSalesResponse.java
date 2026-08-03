package com.bunshik.admin.dto;

import lombok.Data;

@Data
public class MenuSalesResponse {

    private int menuId;
    private String menuName;
    private int quantity;
    private long totalSales;
}
