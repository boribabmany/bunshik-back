package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOptionRequestDto {

    private String optionName;
    private Integer optionPrice;
    private String optionImage;
    private Boolean optionIsAvailable;

}