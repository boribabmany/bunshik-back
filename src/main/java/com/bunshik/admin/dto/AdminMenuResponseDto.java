package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMenuResponseDto {
    // 메뉴 번호
    private Long menuId;
    // 메뉴명
    private String menuName;
    // 카테고리
    private String category;
    // 가격
    private Integer price;
    // 이미지
    private String imageUrl;
    // 판매 여부
    private Boolean isAvailable;
    // 화면에 표시할 상태
    private String status;
}