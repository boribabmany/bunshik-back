package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderMapper orderMapper;

    public OrderResponseDto createOrder(OrderCreateRequestDto request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("주문 항목이 비어있습니다.");
        }

        Map<Integer, Integer> menuPriceCache = new HashMap<>();
        Map<Integer, Integer> optionPriceCache = new HashMap<>();

        int totalPrice = calculateTotalPrice(request, menuPriceCache, optionPriceCache);

        orderMapper.insertOrder(request.getOrderType(), totalPrice);

        Integer orderId = orderMapper.getLastOrderId();

        orderMapper.updateOrderNumber(orderId, String.valueOf(orderId));

        saveOrderItems(orderId, request, menuPriceCache, optionPriceCache);

        return OrderResponseDto.builder()
                .status("대기")
                .orderId(orderId)
                .orderNumber(String.valueOf(orderId))
                .message("주문이 생성되었습니다. 결제를 진행해주세요.")
                .build();
    }

    private int calculateTotalPrice(OrderCreateRequestDto request,
                                    Map<Integer, Integer> menuPriceCache,
                                    Map<Integer, Integer> optionPriceCache) {

        int totalPrice = 0;

        for (OrderItemDto item : request.getItems()) {

            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
            }

            int menuPrice = getMenuPriceCached(item.getMenuId(), menuPriceCache);
            int optionPrice = 0;

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    optionPrice += getOptionPriceCached(optionId, optionPriceCache);
                }
            }

            totalPrice += (menuPrice + optionPrice) * item.getQuantity();
        }

        return totalPrice;
    }

    private void saveOrderItems(Integer orderId, OrderCreateRequestDto request,
                                Map<Integer, Integer> menuPriceCache,
                                Map<Integer, Integer> optionPriceCache) {

        for (OrderItemDto item : request.getItems()) {

            int menuPrice = getMenuPriceCached(item.getMenuId(), menuPriceCache);
            int optionPrice = 0;

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    optionPrice += getOptionPriceCached(optionId, optionPriceCache);
                }
            }

            orderMapper.insertOrderItem(orderId, item.getMenuId(), item.getQuantity(), menuPrice + optionPrice);

            Integer orderItemId = orderMapper.getLastOrderItemId();

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    orderMapper.insertOrderItemOption(orderItemId, optionId);
                }
            }
        }
    }

    private int getMenuPriceCached(Integer menuId, Map<Integer, Integer> cache) {
        return cache.computeIfAbsent(menuId, id -> {
            Integer price = orderMapper.getMenuPrice(id);
            if (price == null) {
                throw new IllegalArgumentException("존재하지 않는 메뉴입니다: " + id);
            }
            return price;
        });
    }

    private int getOptionPriceCached(Integer optionId, Map<Integer, Integer> cache) {
        return cache.computeIfAbsent(optionId, id -> {
            Integer price = orderMapper.getOptionPrice(id);
            if (price == null) {
                throw new IllegalArgumentException("존재하지 않는 옵션입니다: " + id);
            }
            return price;
        });
    }
}