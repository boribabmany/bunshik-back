package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PrintItemDto {

    @JsonProperty("menu_name")
    private String menuName;

    private Integer quantity;

    private Integer price;
}