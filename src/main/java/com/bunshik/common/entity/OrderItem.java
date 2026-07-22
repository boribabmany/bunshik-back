package com.bunshik.common.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItem {

    private Long orderItemId;
    private Long orderId;
    private Long menuId;
    private Integer quantity;
    private Integer price;
}