package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Order {

    private Integer orderId;
    private String orderNumber;
    private String orderType;  // "매장" | "포장"
    private Integer totalPrice;
    private String orderStatus;  // "접수" | "조리중" | "완료" | "취소"
    private LocalDateTime createdAt;
}