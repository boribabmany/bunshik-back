package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Menu {

    private Long menuId;
    private String menuName;
    private String menuNameEn;
    private Integer price;
    private String category;
    private String imageUrl;
    private String description;
    private String descriptionEn;
    private Boolean isAvailable;
    private Boolean effectiveAvailable;
    private Boolean isVisible;      // 추가
    private String soldOutReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
