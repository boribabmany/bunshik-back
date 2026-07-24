package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.kiosk.dto.PaymentRequestDto;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ApiResponse<PaymentResponseDto> processPayment(
            @RequestBody PaymentRequestDto request) {

        PaymentResponseDto response = paymentService.processPayment(request);

        return ApiResponse.success(response);
    }
}