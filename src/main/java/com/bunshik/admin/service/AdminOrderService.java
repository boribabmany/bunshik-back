package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOrderDetailResponseDto;
import com.bunshik.admin.dto.AdminOrderItemResponseDto;
import com.bunshik.admin.dto.AdminOrderItemRowDto;
import com.bunshik.admin.dto.AdminOrderOptionResponseDto;
import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper orderMapper;
    private final AdminHistoryMapper adminHistoryMapper;

    // 주문 전체 조회
    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    // 기존 주문 한 건 조회
    // 상태 변경과 취소에서 계속 사용
    public Order findById(Integer orderId) {
        return orderMapper.findById(orderId);
    }

    // 주문 상세 조회: 주문 기본정보 + 메뉴 + 옵션
    public AdminOrderDetailResponseDto findDetailById(Integer orderId) {

        // 주문 기본정보 조회
        Order order = orderMapper.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다.");
        }

        // 주문 메뉴와 옵션 JOIN 결과 조회
        List<AdminOrderItemRowDto> rows =
                orderMapper.findItemsByOrderId(orderId);

        // 같은 주문 메뉴를 orderItemId 기준으로 묶기
        Map<Integer, AdminOrderItemResponseDto> itemMap =
                new LinkedHashMap<>();

        for (AdminOrderItemRowDto row : rows) {

            AdminOrderItemResponseDto item =
                    itemMap.get(row.getOrderItemId());

            // 아직 등록되지 않은 주문 메뉴라면 생성
            if (item == null) {
                item = new AdminOrderItemResponseDto();

                item.setOrderItemId(row.getOrderItemId());
                item.setMenuName(row.getMenuName());
                item.setQuantity(row.getQuantity());
                item.setUnitPrice(row.getUnitPrice());
                item.setOptions(new ArrayList<>());

                itemMap.put(row.getOrderItemId(), item);
            }

            // 옵션이 있는 경우에만 추가
            if (row.getOptionId() != null) {
                AdminOrderOptionResponseDto option =
                        new AdminOrderOptionResponseDto();

                option.setOptionId(row.getOptionId());
                option.setOptionName(row.getOptionName());
                option.setOptionPrice(row.getOptionPrice());

                item.getOptions().add(option);
            }
        }

        // 최종 상세 응답 생성
        AdminOrderDetailResponseDto detail =
                new AdminOrderDetailResponseDto();

        detail.setOrderId(order.getOrderId());
        detail.setOrderNumber(order.getOrderNumber());
        detail.setOrderType(order.getOrderType());
        detail.setTotalPrice(order.getTotalPrice());
        detail.setOrderStatus(order.getOrderStatus());
        detail.setCreatedAt(order.getCreatedAt());
        detail.setItems(new ArrayList<>(itemMap.values()));

        return detail;
    }

    // 주문 검색
    public List<Order> search(AdminOrderSearchRequestDto dto) {
        return orderMapper.search(dto);
    }

    // 주문 상태 변경
    public int updateStatus(
            Integer orderId,
            AdminOrderStatusRequestDto dto
    ) {

        // 기존 주문 조회
        Order order = orderMapper.findById(orderId);

        // 상태 변경
        order.setOrderStatus(dto.getOrderStatus());

        int result = orderMapper.updateStatus(order);

        if (result > 0) {
            saveHistory(
                    "주문 상태 변경",
                    "주문번호 " + order.getOrderNumber()
                            + " 상태가 "
                            + dto.getOrderStatus()
                            + "(으)로 변경되었습니다."
            );
        }

        return result;
    }

    // 주문 취소
    public int cancel(Integer orderId) {

        // 취소 전에 주문 조회
        Order order = orderMapper.findById(orderId);

        int result = orderMapper.cancel(orderId);

        if (result > 0) {
            saveHistory(
                    "주문 취소",
                    "주문번호 "
                            + order.getOrderNumber()
                            + "가 취소되었습니다."
            );
        }

        return result;
    }

    // 변경 내역 저장
    private void saveHistory(
            String title,
            String description
    ) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}