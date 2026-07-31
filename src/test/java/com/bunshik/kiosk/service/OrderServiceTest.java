package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

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

        orderService.createOrder(request);

        verify(orderMapper).insertOrderItemSetComponents(200, 10);
    }
}
