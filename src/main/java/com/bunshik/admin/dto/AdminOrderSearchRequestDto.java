package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOrderSearchRequestDto {
    // 조회 날짜
    private String date;
    // 주문 유형 (매장 / 포장)
    private String orderType;
    // 주문 상태 (접수 / 조리중 / 완료 / 취소)
    private String orderStatus;
}