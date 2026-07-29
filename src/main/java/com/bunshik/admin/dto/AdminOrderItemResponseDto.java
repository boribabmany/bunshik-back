package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AdminOrderItemResponseDto {

    private Integer orderItemId;
    private String menuName;
    private Integer quantity;
    private Integer unitPrice;

    private List<AdminOrderOptionResponseDto> options;
}