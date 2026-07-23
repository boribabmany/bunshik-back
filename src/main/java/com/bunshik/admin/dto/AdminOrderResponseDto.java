package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminOrderResponseDto {

    private Integer orderId;
    private String orderNumber;
    private String orderType;
    private Integer totalPrice;
    private String orderStatus;
    private LocalDateTime createdAt;
}