package com.bunshik.kiosk.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintJobInfo {

    private Long printJobId;
    private Integer orderId;
    private String type;
    private String orderNumber;
    private String orderType;
    private String paymentMethod;
    private Integer totalPrice;
    private String status;
}