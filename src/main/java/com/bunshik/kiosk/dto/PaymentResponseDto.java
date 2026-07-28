package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponseDto {

    private String status;

    @JsonProperty("fail_type")
    private String failType;

    @JsonProperty("fail_reason")
    private String failReason;
}