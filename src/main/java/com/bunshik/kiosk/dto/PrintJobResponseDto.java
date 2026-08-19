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

    private List<PrintItemDto> items;

    @JsonProperty("total_price")
    private Integer totalPrice;

    private String status;
}