package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossFailRequestDto {

    @NotNull(message = "주문 번호가 필요합니다.")
    @JsonProperty("order_id")
    private Integer orderId;

    private String message;
}