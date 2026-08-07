package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.mapper.OrderMapper;
import com.bunshik.kiosk.mapper.SetGroupInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderSnapshotsCurrentSetComponents() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setOptionIds(List.of());

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getLastOrderId()).thenReturn(100);
        when(orderMapper.getLastOrderItemId()).thenReturn(200);
        when(orderMapper.getSetComponentInfo(10)).thenReturn(
                List.of(component(1, "떡볶이"))
        );

        orderService.createOrder(request);

        verify(orderMapper).insertOrderItemSetComponent(
                200,
                1,
                "떡볶이"
        );
    }

    @Test
    void createOrderRejectsEmptyItems() {
        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("주문 항목이 비어있습니다.");

        verify(orderMapper, never()).insertOrder(anyString(), anyInt());
    }

    @Test
    void createOrderRejectsNonPositiveQuantity() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(0);

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");

        verify(orderMapper, never()).insertOrder(anyString(), anyInt());
    }

    @Test
    void createOrderRejectsNonExistentMenu() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(999);
        item.setQuantity(1);

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(999)).thenReturn(null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 메뉴입니다: 999");
    }

    @Test
    void createOrderRejectsMenuThatIsNotOrderable() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 주문할 수 없는 메뉴입니다: 10");
    }

    @Test
    void createOrderRejectsNonExistentOption() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setOptionIds(List.of(55));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getOptionPrice(55)).thenReturn(null);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 옵션입니다: 55");
    }

    @Test
    void createOrderRejectsOptionThatIsNotOrderable() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setOptionIds(List.of(55));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getOptionPrice(55)).thenReturn(500);
        when(orderMapper.isOptionOrderable(55)).thenReturn(false);

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 선택할 수 없는 옵션입니다: 55");
    }

    @Test
    void createOrderCalculatesTotalPriceWithOptionsAndQuantity() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(2);
        item.setOptionIds(List.of(55));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("포장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(4000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getOptionPrice(55)).thenReturn(1000);
        when(orderMapper.isOptionOrderable(55)).thenReturn(true);
        when(orderMapper.getLastOrderId()).thenReturn(100);
        when(orderMapper.getLastOrderItemId()).thenReturn(200);
        when(orderMapper.getSetComponentInfo(10)).thenReturn(List.of());

        OrderResponseDto response = orderService.createOrder(request);

        // (4000 + 1000) * 2 = 10000
        verify(orderMapper).insertOrder("포장", 10000);
        assertThat(response.getStatus()).isEqualTo("대기");
        assertThat(response.getOrderId()).isEqualTo(100);
        assertThat(response.getOrderNumber()).isEqualTo("100");
    }

    @Test
    void createOrderRejectsWrongNumberOfSelectGroupChoices() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setComponentMenuIds(List.of());

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getSetComponentInfo(10)).thenReturn(List.of(
                selectableComponent("김밥선택", 1, 1, "야채김밥", 0, true),
                selectableComponent("김밥선택", 2, 1, "참치김밥", 500, true)
        ));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("김밥선택에서 정확히 1개를 선택해야 합니다.");
    }

    @Test
    void createOrderRejectsUnavailableSelectedComponent() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setComponentMenuIds(List.of(2));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getSetComponentInfo(10)).thenReturn(List.of(
                selectableComponent("김밥선택", 1, 1, "야채김밥", 0, true),
                selectableComponent("김밥선택", 2, 1, "참치김밥", 500, false)
        ));

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 선택할 수 없는 메뉴입니다: 2");
    }

    @Test
    void createOrderAddsSelectGroupExtraPriceForValidChoice() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);
        item.setComponentMenuIds(List.of(2));

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));

        when(orderMapper.getMenuPrice(10)).thenReturn(11000);
        when(orderMapper.isMenuOrderable(10)).thenReturn(true);
        when(orderMapper.getLastOrderId()).thenReturn(100);
        when(orderMapper.getLastOrderItemId()).thenReturn(200);
        when(orderMapper.getSetComponentInfo(10)).thenReturn(List.of(
                selectableComponent("김밥선택", 1, 1, "야채김밥", 0, true),
                selectableComponent("김밥선택", 2, 1, "참치김밥", 500, true)
        ));

        orderService.createOrder(request);

        // 11000(메뉴가) + 500(선택 추가금) = 11500
        verify(orderMapper).insertOrder("매장", 11500);
        verify(orderMapper).insertOrderItemSetComponent(200, 2, "참치김밥");
    }

    @Test
    void cancelOrderCancelsOrderInPaymentPendingStatus() {
        when(orderMapper.getOrderStatus(1)).thenReturn("결제대기");

        orderService.cancelOrder(1);

        verify(orderMapper).cancelOrder(1);
    }

    @Test
    void cancelOrderRejectsNonExistentOrder() {
        when(orderMapper.getOrderStatus(99)).thenReturn(null);

        assertThatThrownBy(() -> orderService.cancelOrder(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문입니다: 99");

        verify(orderMapper, never()).cancelOrder(any());
    }

    @Test
    void cancelOrderRejectsOrderNotInPaymentPendingStatus() {
        when(orderMapper.getOrderStatus(1)).thenReturn("접수");

        assertThatThrownBy(() -> orderService.cancelOrder(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 대기 중인 주문만 취소할 수 있습니다.");

        verify(orderMapper, never()).cancelOrder(any());
    }

    private SetGroupInfo component(
            Integer componentMenuId,
            String componentMenuName
    ) {
        SetGroupInfo component = new SetGroupInfo();
        component.setComponentMenuId(componentMenuId);
        component.setComponentMenuName(componentMenuName);
        component.setExtraPrice(0);
        component.setIsAvailable(true);
        return component;
    }

    private SetGroupInfo selectableComponent(
            String selectGroup,
            Integer componentMenuId,
            Integer groupMaxSelect,
            String componentMenuName,
            Integer extraPrice,
            boolean isAvailable
    ) {
        SetGroupInfo component = new SetGroupInfo();
        component.setSelectGroup(selectGroup);
        component.setComponentMenuId(componentMenuId);
        component.setGroupMaxSelect(groupMaxSelect);
        component.setComponentMenuName(componentMenuName);
        component.setExtraPrice(extraPrice);
        component.setIsAvailable(isAvailable);
        return component;
    }
}
