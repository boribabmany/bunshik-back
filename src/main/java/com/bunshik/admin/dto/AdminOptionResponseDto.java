package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminOptionResponseDto {
    // 옵션 번호
    private Long optionId;
    // 옵션명
    private String optionName;
    // 추가 금액
    private Integer optionPrice;
    // 이미지
    private String optionImage;
    // 판매 여부
    private Boolean optionIsAvailable;
    // 화면 표시용
    private String status;
}