package com.bunshik.kiosk.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuResponseDto {

    private Long menuId;
    private String menuName;
    private Integer price;
    private String category;
    private String imageUrl;
    private Boolean isAvailable;
}
