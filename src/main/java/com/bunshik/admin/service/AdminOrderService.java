package com.bunshik.admin.service;

import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper adminOrderMapper;

    // 전체 조회
    public List<Order> findAll() {
        return adminOrderMapper.findAll();
    }

    // 상세 조회
    public Order findById(Integer orderId) {
        return adminOrderMapper.findById(orderId);
    }

    // 등록
    public int insert(Order order) {
        return adminOrderMapper.insert(order);
    }

    // 수정
    public int update(Order order) {
        return adminOrderMapper.update(order);
    }

    // 삭제
    public int delete(Integer orderId) {
        return adminOrderMapper.delete(orderId);
    }
}