package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminLoginRequestDto {

    private String username;

    private String password;
}