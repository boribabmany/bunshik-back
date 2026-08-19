package com.bunshik.kiosk.mapper;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintItemInfo {

    private String menuName;
    private Integer quantity;
    private Integer priceAtOrder;
}