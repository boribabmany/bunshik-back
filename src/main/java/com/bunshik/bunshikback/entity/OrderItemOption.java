package com.bunshik.bunshikback.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemOption {

    private Long orderItemOptionId;
    private Long orderItemId;
    private Long optionId;
}