package com.bunshik.admin.dto;

import lombok.Data;

@Data
public class PopularMenuResponse {

    private String menuName;
    private int orderCount;
}