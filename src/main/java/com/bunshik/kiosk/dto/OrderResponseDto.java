package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponseDto {

    private String status;

    @JsonProperty("order_id")
    private Integer orderId;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("fail_reason")
    private String failReason;

    private String message;
}