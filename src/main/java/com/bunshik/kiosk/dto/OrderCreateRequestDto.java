package com.bunshik.kiosk.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderCreateRequestDto {

    @NotEmpty(message = "주문 항목이 비어있습니다.")
    @Valid
    private List<OrderItemDto> items;

    @NotBlank(message = "주문 타입을 선택해주세요.")
    @Pattern(regexp = "매장|포장", message = "주문 타입은 매장 또는 포장만 가능합니다.")
    @JsonProperty("order_type")
    private String orderType;

}