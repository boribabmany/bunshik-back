package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PrintJobResponseDto {

    private Long id;

    private String type;

    @JsonProperty("order_number")
    private String orderNumber;

    @JsonProperty("order_type")
    private String orderType;

    @JsonProperty("payment_method")
    private String paymentMethod;

    private List<PrintItemDto> items;

    @JsonProperty("total_price")
    private Integer totalPrice;

    private String status;
}