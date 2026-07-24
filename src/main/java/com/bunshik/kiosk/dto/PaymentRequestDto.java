package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {

    @NotNull(message = "주문 번호가 필요합니다.")
    @JsonProperty("order_id")
    private Integer orderId;

    @NotBlank(message = "결제 수단을 선택해주세요.")
    @JsonProperty("payment_method")
    private String paymentMethod;
}