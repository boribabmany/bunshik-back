package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Payment {

    private Long paymentId;
    private Long orderId;
    private Integer amount;
    private String paymentMethod;  // "카드" | "현금" | "간편결제"
    private String paymentStatus;  // "성공" | "카드오류" | "거절" | "시스템오류" | "취소"
    private String failReason;
    private LocalDateTime attemptedAt;
}