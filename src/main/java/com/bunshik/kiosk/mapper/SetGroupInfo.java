package com.bunshik.kiosk.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SetGroupInfo {
    private String selectGroup;
    private Integer componentMenuId;
    private String componentMenuName;   // 추가
    private Integer groupMaxSelect;
    private Integer extraPrice;
    private Boolean isAvailable;
}