package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminUser {

    private Integer id;
    private String username;
    private String passwordHash;
    private Boolean isActive;
    private LocalDateTime createdAt;
}