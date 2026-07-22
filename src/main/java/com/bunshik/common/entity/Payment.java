package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Payment {

    private Integer paymentId;
    private Integer orderId;
    private Integer amount;
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime paidAt;
}