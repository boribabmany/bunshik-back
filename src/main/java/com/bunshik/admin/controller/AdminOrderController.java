package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.service.AdminOrderService;
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
    public List<Order> findAll() {
        return adminOrderService.findAll();
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public Order findById(@PathVariable Integer orderId) {
        return adminOrderService.findById(orderId);
    }

    // 주문 검색
    @GetMapping("/search")
    public List<Order> search(AdminOrderSearchRequestDto dto) {
        return adminOrderService.search(dto);
    }

    // 주문 상태 변경
    @PatchMapping("/{orderId}/status")
    public int updateStatus(@PathVariable Integer orderId,
                            @RequestBody AdminOrderStatusRequestDto dto) {

        return adminOrderService.updateStatus(orderId, dto);
    }

    // 주문 취소
    @PatchMapping("/{orderId}/cancel")
    public int cancel(@PathVariable Integer orderId) {
        return adminOrderService.cancel(orderId);
    }
}