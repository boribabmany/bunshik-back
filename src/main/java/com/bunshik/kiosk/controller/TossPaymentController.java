package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.dto.TossConfirmRequestDto;
import com.bunshik.kiosk.dto.TossFailRequestDto;
import com.bunshik.kiosk.service.TossPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/toss")
@RequiredArgsConstructor
public class TossPaymentController {

    private final TossPaymentService tossPaymentService;

    @PostMapping("/confirm")
    public ApiResponse<PaymentResponseDto> confirm(@Valid @RequestBody TossConfirmRequestDto request) {
        return ApiResponse.success(tossPaymentService.confirm(request));
    }

    @PostMapping("/fail")
    public ApiResponse<Void> fail(@Valid @RequestBody TossFailRequestDto request) {
        tossPaymentService.fail(request);
        return ApiResponse.success(null, "결제가 취소되었습니다.");
    }
}