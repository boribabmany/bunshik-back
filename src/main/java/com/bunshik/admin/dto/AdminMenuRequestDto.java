package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMenuRequestDto {

    private String menuName;
    private String menuNameEn;
    private Integer price;
    private String category;
    private String imageUrl;
    private String description;
    private String descriptionEn;
    private Boolean isAvailable;
    private String soldOutReason;
}