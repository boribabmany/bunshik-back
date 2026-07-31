package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.mapper.OrderMapper;
import com.bunshik.kiosk.mapper.SetGroupInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    // 주문 취소 (손님이 결제를 포기했을 때)
    public void cancelOrder(Integer orderId) {

        String currentStatus = orderMapper.getOrderStatus(orderId);

        if (currentStatus == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다: " + orderId);
        }

        // 결제대기 상태인 주문만 손님이 직접 취소할 수 있음
        // (이미 접수/조리중/완료된 주문은 여기서 취소 불가)
        if (!"결제대기".equals(currentStatus)) {
            throw new IllegalStateException("결제 대기 중인 주문만 취소할 수 있습니다.");
        }

        orderMapper.cancelOrder(orderId);
    }

    // 주문 총액 계산 (메뉴가격 + 옵션가격 + 세트 선택 추가금액) x 수량, 항목별 합산
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

            int setExtraPrice = validateAndCalculateSetComponents(item.getMenuId(), item.getComponentMenuIds());

            totalPrice += (menuPrice + optionPrice + setExtraPrice) * item.getQuantity();
        }

        return totalPrice;
    }

    // 세트 그룹 선택 검증(그룹당 정확히 group_max_select개) + 선택된 후보들의 추가금액 합산
    private int validateAndCalculateSetComponents(Integer setMenuId, List<Integer> componentMenuIds) {

        List<SetGroupInfo> groupInfos = orderMapper.getSetGroupInfo(setMenuId);

        if (groupInfos.isEmpty()) {
            // 선택형 그룹이 없는 메뉴(일반 메뉴, 또는 떡순튀세트 같은 고정형 세트)
            return 0;
        }

        List<Integer> selectedIds = componentMenuIds != null ? componentMenuIds : List.of();

        // 그룹별로 후보 정리 (예: "김밥선택" -> [야채김밥, 참치김밥], "음료선택" -> [콜라, 사이다])
        Map<String, List<SetGroupInfo>> byGroup = groupInfos.stream()
                .collect(Collectors.groupingBy(SetGroupInfo::getSelectGroup));

        int extraPriceSum = 0;

        for (Map.Entry<String, List<SetGroupInfo>> entry : byGroup.entrySet()) {

            String groupName = entry.getKey();
            List<SetGroupInfo> candidates = entry.getValue();
            int maxSelect = candidates.get(0).getGroupMaxSelect();

            List<Integer> candidateIds = candidates.stream()
                    .map(SetGroupInfo::getComponentMenuId)
                    .toList();

            // 요청에 들어온 id 중, 이 그룹에 속하는 것만 필터링
            List<Integer> selectedInGroup = selectedIds.stream()
                    .filter(candidateIds::contains)
                    .toList();

            if (selectedInGroup.size() != maxSelect) {
                throw new IllegalArgumentException(
                        groupName + "에서 정확히 " + maxSelect + "개를 선택해야 합니다.");
            }

            for (Integer selectedId : selectedInGroup) {
                SetGroupInfo picked = candidates.stream()
                        .filter(c -> c.getComponentMenuId().equals(selectedId))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 선택입니다: " + selectedId));

                if (!picked.getIsAvailable()) {
                    throw new IllegalArgumentException("현재 선택할 수 없는 메뉴입니다: " + selectedId);
                }

                extraPriceSum += picked.getExtraPrice();
            }
        }

        return extraPriceSum;
    }

    // 주문 항목 + 옵션 + 세트 선택 구성 저장
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

            // 세트 선택 추가금액도 price_at_order에 포함되도록 다시 계산
            int setExtraPrice = validateAndCalculateSetComponents(item.getMenuId(), item.getComponentMenuIds());

            orderMapper.insertOrderItem(
                    orderId,
                    item.getMenuId(),
                    item.getQuantity(),
                    menuPrice + optionPrice + setExtraPrice
            );

            Integer orderItemId = orderMapper.getLastOrderItemId();

            if (item.getOptionIds() != null) {
                for (Integer optionId : item.getOptionIds()) {
                    orderMapper.insertOrderItemOption(orderItemId, optionId);
                }
            }

            // 세트에서 고른 구성 메뉴 기록
            if (item.getComponentMenuIds() != null && !item.getComponentMenuIds().isEmpty()) {

                List<SetGroupInfo> groupInfos = orderMapper.getSetGroupInfo(item.getMenuId());

                Map<Integer, String> nameById = groupInfos.stream()
                        .collect(Collectors.toMap(SetGroupInfo::getComponentMenuId, SetGroupInfo::getComponentMenuName));

                for (Integer componentMenuId : item.getComponentMenuIds()) {
                    String componentMenuName = nameById.get(componentMenuId);
                    orderMapper.insertOrderItemSetComponent(orderItemId, componentMenuId, componentMenuName);
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
            Boolean orderable = orderMapper.isMenuOrderable(id);
            if (orderable == null || !orderable) {
                throw new IllegalArgumentException("현재 주문할 수 없는 메뉴입니다: " + id);
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
            Boolean orderable = orderMapper.isOptionOrderable(id);
            if (orderable == null || !orderable) {
                throw new IllegalArgumentException("현재 선택할 수 없는 옵션입니다: " + id);
            }
            return price;
        });
    }
}