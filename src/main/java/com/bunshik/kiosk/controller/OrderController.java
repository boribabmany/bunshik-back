package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ApiResponse<OrderResponseDto> createOrder(
            @Valid @RequestBody OrderCreateRequestDto request) {

        OrderResponseDto response = orderService.createOrder(request);

        return ApiResponse.success(response);
    }
}