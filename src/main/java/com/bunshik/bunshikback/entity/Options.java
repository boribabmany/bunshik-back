package com.bunshik.bunshikback.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Options {

    private Long optionId;
    private String optionName;
    private Integer optionPrice;
    private String optionImage;
    private Boolean optionIsAvailable;
}