package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderItemDto {

    @JsonProperty("menu_id")
    private Integer menuId;

    private Integer quantity;

    @JsonProperty("option_ids")
    private List<Integer> optionIds;
}