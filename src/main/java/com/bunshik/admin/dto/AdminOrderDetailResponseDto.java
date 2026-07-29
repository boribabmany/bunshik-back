package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AdminOrderDetailResponseDto {

    private Integer orderId;
    private String orderNumber;
    private String orderType;
    private Integer totalPrice;
    private String orderStatus;
    private LocalDateTime createdAt;

    private List<AdminOrderItemResponseDto> items;
}