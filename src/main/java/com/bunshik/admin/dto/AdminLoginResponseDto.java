package com.bunshik.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponseDto {

    private Integer id;
    private String username;
    private String accessToken;
    private String tokenType;

}