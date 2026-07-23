package com.bunshik.admin.controller;

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

    // 주문 등록
    @PostMapping
    public int insert(@RequestBody Order order) {
        return adminOrderService.insert(order);
    }

    // 주문 수정
    @PutMapping("/{orderId}")
    public int update(@PathVariable Integer orderId,
                      @RequestBody Order order) {

        order.setOrderId(orderId);
        return adminOrderService.update(order);
    }

    // 주문 삭제
    @DeleteMapping("/{orderId}")
    public int delete(@PathVariable Integer orderId) {
        return adminOrderService.delete(orderId);
    }
}