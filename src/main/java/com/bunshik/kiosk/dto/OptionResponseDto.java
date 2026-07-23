package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OptionResponseDto {

    @JsonProperty("option_id")
    private Long optionId;

    @JsonProperty("option_name")
    private String optionName;

    @JsonProperty("option_name_en")
    private String optionNameEn;

    @JsonProperty("option_price")
    private Integer optionPrice;

    @JsonProperty("option_image")
    private String optionImage;
}