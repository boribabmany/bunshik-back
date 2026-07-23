package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper orderMapper;

    // 주문 전체 조회
    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    // 주문 상세 조회
    public Order findById(Integer orderId) {
        return orderMapper.findById(orderId);
    }

    // 주문 검색
    public List<Order> search(AdminOrderSearchRequestDto dto) {
        return orderMapper.search(dto);
    }

    // 주문 상태 변경
    public int updateStatus(Order order) {
        return orderMapper.updateStatus(order);
    }

    // 주문 취소
    public int cancel(Integer orderId) {
        return orderMapper.cancel(orderId);
    }
}