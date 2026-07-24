package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderItemDto {

    @NotNull
    @JsonProperty("menu_id")
    private Integer menuId;

    @NotNull
    @Min(1)
    private Integer quantity;

    @JsonProperty("option_ids")
    private List<Integer> optionIds;
}