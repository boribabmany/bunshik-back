package com.bunshik.kiosk.controller;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import com.bunshik.common.config.SecurityConfig;
import com.bunshik.kiosk.dto.OrderCreateRequestDto;
import com.bunshik.kiosk.dto.OrderItemDto;
import com.bunshik.kiosk.dto.OrderResponseDto;
import com.bunshik.kiosk.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, AdminJwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Boot 자동구성 ObjectMapper 빈에 의존하지 않고 테스트 자체 인스턴스를 사용
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderReturnsCreatedOrder() throws Exception {
        OrderResponseDto response = OrderResponseDto.builder()
                .status("대기")
                .orderId(100)
                .orderNumber("100")
                .message("주문이 생성되었습니다. 결제를 진행해주세요.")
                .build();

        when(orderService.createOrder(any())).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("대기"))
                .andExpect(jsonPath("$.data.order_id").value(100))
                .andExpect(jsonPath("$.data.order_number").value("100"));
    }

    @Test
    void createOrderRejectsEmptyItems() throws Exception {
        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of());

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문 항목이 비어있습니다."));

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrderRejectsInvalidOrderType() throws Exception {
        OrderCreateRequestDto request = validOrderRequest();
        request.setOrderType("배달");

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문 타입은 매장 또는 포장만 가능합니다."));

        verifyNoInteractions(orderService);
    }

    @Test
    void createOrderPropagatesServiceValidationErrorAsBadRequest() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new IllegalArgumentException("존재하지 않는 메뉴입니다: 999"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validOrderRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 메뉴입니다: 999"));
    }

    @Test
    void cancelOrderReturnsSuccessMessage() throws Exception {
        mockMvc.perform(patch("/api/orders/{orderId}/cancel", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("주문이 취소되었습니다."));

        verify(orderService).cancelOrder(1);
    }

    @Test
    void cancelOrderPropagatesStateErrorAsBadRequest() throws Exception {
        doThrow(new IllegalStateException("결제 대기 중인 주문만 취소할 수 있습니다."))
                .when(orderService).cancelOrder(1);

        mockMvc.perform(patch("/api/orders/{orderId}/cancel", 1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 대기 중인 주문만 취소할 수 있습니다."));
    }

    private OrderCreateRequestDto validOrderRequest() {
        OrderItemDto item = new OrderItemDto();
        item.setMenuId(10);
        item.setQuantity(1);

        OrderCreateRequestDto request = new OrderCreateRequestDto();
        request.setOrderType("매장");
        request.setItems(List.of(item));
        return request;
    }
}
