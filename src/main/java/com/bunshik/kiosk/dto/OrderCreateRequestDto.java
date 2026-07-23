package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequestDto {

    private List<OrderItemDto> items;

    @JsonProperty("order_type")
    private String orderType;

    @JsonProperty("payment_method")
    private String paymentMethod;
}