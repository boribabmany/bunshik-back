package com.bunshik.bunshikback.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Menus {

    private Long menuId;
    private String menuName;
    private Integer price;
    private String category;
    private String imageUrl;
    private String description;
    private Boolean isAvailable;
    private String soldOutReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}