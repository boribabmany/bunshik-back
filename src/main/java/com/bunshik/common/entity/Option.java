package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Option {

    private Long optionId;
    private String optionName;
    private String optionNameEn;
    private Integer optionPrice;
    private String optionImage;
    private Boolean optionIsAvailable;
    private Boolean isVisible;
}