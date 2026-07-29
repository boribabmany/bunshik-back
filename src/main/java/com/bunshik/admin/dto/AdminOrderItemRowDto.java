package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOrderItemRowDto {

    private Integer orderItemId;
    private String menuName;
    private Integer quantity;
    private Integer unitPrice;

    private Integer optionId;
    private String optionName;
    private Integer optionPrice;
}