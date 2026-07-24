package com.bunshik.kiosk.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentInfo {
    private Integer orderId;
    private String orderNumber;
    private Integer totalPrice;
    private String orderStatus;
}