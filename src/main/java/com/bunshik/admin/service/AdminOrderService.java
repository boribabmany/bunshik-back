package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper orderMapper;
    private final AdminHistoryMapper adminHistoryMapper;

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
    public int updateStatus(Integer orderId, AdminOrderStatusRequestDto dto) {

        // 기존 주문 조회
        Order order = orderMapper.findById(orderId);

        // 상태 변경
        order.setOrderStatus(dto.getOrderStatus());

        int result = orderMapper.updateStatus(order);

        if (result > 0) {
            saveHistory(
                    "주문 상태 변경",
                    "주문번호 " + order.getOrderNumber()
                            + " 상태가 " + dto.getOrderStatus() + "(으)로 변경되었습니다."
            );
        }

        return result;
    }

    // 주문 취소
    public int cancel(Integer orderId) {

        // 삭제 전에 주문 조회
        Order order = orderMapper.findById(orderId);

        int result = orderMapper.cancel(orderId);

        if (result > 0) {
            saveHistory(
                    "주문 취소",
                    "주문번호 " + order.getOrderNumber() + "가 취소되었습니다."
            );
        }

        return result;
    }

    // 변경 내역 저장
    private void saveHistory(String title, String description) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}