package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderItemDto {

    @NotNull(message = "메뉴를 선택해주세요.")
    @JsonProperty("menu_id")
    private Integer menuId;

    @NotNull(message = "수량을 입력해주세요.")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;

    @Size(max = 2, message = "옵션은 최대 2개까지 선택할 수 있습니다.")
    @JsonProperty("option_ids")
    private List<Integer> optionIds;

    @JsonProperty("component_menu_ids")
    private List<Integer> componentMenuIds;
}