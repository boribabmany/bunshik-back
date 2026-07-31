package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetMenuComponentDto {
    private Long componentMenuId;
    private String menuName;
    private String menuNameEn;
    private String category;
    private Boolean isAvailable;
    private Boolean isVisible;
    private String selectGroup;
    private Integer groupMaxSelect;
    private Integer extraPrice;
}
