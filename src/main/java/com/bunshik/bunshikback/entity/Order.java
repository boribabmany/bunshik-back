package com.bunshik.bunshikback.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Order {

    private Integer orderId;
    private String orderNumber;
    private String orderType;
    private Integer totalPrice;
    private String orderStatus;
    private LocalDateTime createdAt;
}