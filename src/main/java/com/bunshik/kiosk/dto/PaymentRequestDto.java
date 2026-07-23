package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequestDto {

    @JsonProperty("order_id")
    private Integer orderId;

    @JsonProperty("payment_method")
    private String paymentMethod;
}