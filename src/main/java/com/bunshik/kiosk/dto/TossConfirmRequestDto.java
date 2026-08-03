package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TossConfirmRequestDto {

    @NotNull(message = "주문 번호가 필요합니다.")
    @JsonProperty("order_id")
    private Integer orderId;

    @NotBlank(message = "paymentKey가 필요합니다.")
    @JsonProperty("payment_key")
    private String paymentKey;

    @NotBlank(message = "tossOrderId가 필요합니다.")
    @JsonProperty("toss_order_id")
    private String tossOrderId;

    @NotNull(message = "결제 금액이 필요합니다.")
    private Integer amount;

    @NotBlank(message = "결제 수단이 필요합니다.")
    @JsonProperty("payment_method")
    private String paymentMethod; // "토스페이" | "카카오페이"
}