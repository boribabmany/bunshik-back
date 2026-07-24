package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.service.AdminOrderService;
import com.bunshik.common.ApiResponse;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    // 주문 전체 조회
    @GetMapping
    public ApiResponse<List<Order>> findAll() {
        return ApiResponse.success(
                adminOrderService.findAll()
        );
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ApiResponse<Order> findById(
            @PathVariable Integer orderId
    ) {
        return ApiResponse.success(
                adminOrderService.findById(orderId)
        );
    }

    // 주문 검색
    @GetMapping("/search")
    public ApiResponse<List<Order>> search(
            AdminOrderSearchRequestDto dto
    ) {
        return ApiResponse.success(
                adminOrderService.search(dto)
        );
    }

    // 주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public ApiResponse<Integer> updateStatus(
            @PathVariable Integer orderId,
            @RequestBody AdminOrderStatusRequestDto dto
    ) {
        int result = adminOrderService.updateStatus(orderId, dto);

        return ApiResponse.success(
                result,
                "주문 상태가 변경되었습니다."
        );
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public ApiResponse<Integer> cancel(
            @PathVariable Integer orderId
    ) {
        int result = adminOrderService.cancel(orderId);

        return ApiResponse.success(
                result,
                "주문이 취소되었습니다."
        );
    }
}