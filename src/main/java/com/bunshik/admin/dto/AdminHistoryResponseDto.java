package com.bunshik.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AdminHistoryResponseDto {

    private Integer id;
    private Integer adminId;
    private String title;
    private String description;
    private LocalDateTime createdAt;

}