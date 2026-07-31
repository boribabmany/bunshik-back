package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOrderSetComponentRowDto {

    private Integer orderItemId;
    private Integer componentMenuId;
    private String componentMenuName;
}
