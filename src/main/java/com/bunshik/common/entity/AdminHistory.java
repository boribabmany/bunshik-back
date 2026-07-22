package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminHistory {

    private Integer id;
    private Integer adminId;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}