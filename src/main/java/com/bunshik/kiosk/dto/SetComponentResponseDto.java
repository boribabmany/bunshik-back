package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetComponentResponseDto {

    @JsonProperty("component_menu_id")
    private Integer componentMenuId;

    @JsonProperty("component_menu_name")
    private String componentMenuName;

    @JsonProperty("component_menu_name_en")
    private String componentMenuNameEn;

    @JsonProperty("component_image_url")
    private String componentImageUrl;

    @JsonProperty("extra_price")
    private Integer extraPrice;

    @JsonProperty("is_available")
    private Boolean isAvailable;

    @JsonProperty("select_group")
    private String selectGroup;

    @JsonProperty("group_max_select")
    private Integer groupMaxSelect;
}