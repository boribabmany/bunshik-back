package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdminMenuRequestDto {

    private String menuName;
    private String menuNameEn;
    private String menuType;
    private Integer price;
    private String category;
    private String imageUrl;
    private String description;
    private String descriptionEn;
    private Boolean isAvailable;
    private String soldOutReason;
    private List<Long> componentMenuIds = new ArrayList<>();
    private List<SetMenuComponentDto> componentSettings = new ArrayList<>();
}
