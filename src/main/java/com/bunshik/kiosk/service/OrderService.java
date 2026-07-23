package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderResponseDto createOrder(OrderCreateRequestDto request) {

        int totalPrice = calculateTotalPrice(request);

        // 주문 저장 (order_number는 임시값 0)
        orderMapper.insertOrder(
                request.getOrderType(),
                totalPrice
        );

        // 생성된 PK 조회
        Integer orderId = orderMapper.getLastOrderId();

        // order_number를 order_id와 동일하게 수정
        orderMapper.updateOrderNumber(
                orderId,
                String.valueOf(orderId)
        );

        // 주문 상세 저장
        saveOrderItems(orderId, request);

        // 결제 저장
        orderMapper.insertPayment(
                orderId,
                totalPrice,
                request.getPaymentMethod(),
                "성공",
                null
        );

        return OrderResponseDto.builder()
                .status("success")
                .orderNumber(String.valueOf(orderId))
                .message("주문이 완료되었습니다.")
                .build();
    }

    // 총 금액 계산
    private int calculateTotalPrice(OrderCreateRequestDto request) {

        int totalPrice = 0;

        for (OrderItemDto item : request.getItems()) {

            int menuPrice = orderMapper.getMenuPrice(item.getMenuId());

            int optionPrice = 0;

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    optionPrice += orderMapper.getOptionPrice(optionId);
                }
            }

            totalPrice += (menuPrice + optionPrice) * item.getQuantity();
        }

        return totalPrice;
    }

    // 주문상품 저장
    private void saveOrderItems(Integer orderId, OrderCreateRequestDto request) {

        for (OrderItemDto item : request.getItems()) {

            int menuPrice = orderMapper.getMenuPrice(item.getMenuId());

            int optionPrice = 0;

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    optionPrice += orderMapper.getOptionPrice(optionId);
                }
            }

            orderMapper.insertOrderItem(
                    orderId,
                    item.getMenuId(),
                    item.getQuantity(),
                    menuPrice + optionPrice
            );

            Integer orderItemId = orderMapper.getLastOrderItemId();

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    orderMapper.insertOrderItemOption(
                            orderItemId,
                            optionId
                    );
                }
            }
        }
    }
}