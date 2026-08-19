package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintJobRequestDto {

    @NotNull(message = "주문 ID가 필요합니다.")
    @JsonProperty("order_id")
    private Integer orderId;

    @NotBlank(message = "출력 타입이 필요합니다.")
    @Pattern(regexp = "RECEIPT|ORDER_NUMBER", message = "출력 타입은 RECEIPT 또는 ORDER_NUMBER만 가능합니다.")
    private String type;
}