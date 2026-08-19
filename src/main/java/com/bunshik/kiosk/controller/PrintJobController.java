package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.kiosk.dto.PrintJobCompleteRequestDto;
import com.bunshik.kiosk.dto.PrintJobRequestDto;
import com.bunshik.kiosk.dto.PrintJobResponseDto;
import com.bunshik.kiosk.service.PrintJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/print-jobs")
@RequiredArgsConstructor
public class PrintJobController {

    private final PrintJobService printJobService;

    // 손님이 OrderComplete 화면에서 "영수증 출력" / "주문번호만 출력" 버튼 클릭 시 호출
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> request(@Valid @RequestBody PrintJobRequestDto request) {
        printJobService.request(request.getOrderId(), request.getType());
        return ApiResponse.success(null, "출력 요청이 접수되었습니다.");
    }

    // RTOS가 주기적으로 조회하는 대기 중인 출력 작업
    @GetMapping("/pending")
    public ApiResponse<List<PrintJobResponseDto>> pending() {
        return ApiResponse.success(printJobService.pending());
    }

    // RTOS가 영수증/번호표 출력 완료 후 호출
    @PatchMapping("/{id}/complete")
    public ApiResponse<Void> complete(@PathVariable Long id,
                                      @Valid @RequestBody PrintJobCompleteRequestDto request) {
        printJobService.complete(id, request.getResult());
        return ApiResponse.success(null, "출력 완료 처리되었습니다.");
    }
}