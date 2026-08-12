package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOrderDetailResponseDto;
import com.bunshik.admin.dto.AdminOrderItemRowDto;
import com.bunshik.admin.dto.AdminOrderResponseDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.dto.AdminOrderSetComponentRowDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.admin.security.CurrentAdminProvider;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Order;
import com.bunshik.common.entity.Payment;
import com.bunshik.kiosk.service.TossPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminOrderServiceTest {

    @Mock
    private AdminOrderMapper orderMapper;

    @Mock
    private AdminHistoryMapper adminHistoryMapper;

    @Mock
    private CurrentAdminProvider currentAdminProvider;

    @Mock
    private TossPaymentService tossPaymentService;

    @InjectMocks
    private AdminOrderService adminOrderService;

    @Test
    void findAllReturnsMapperResult() {
        AdminOrderResponseDto order = new AdminOrderResponseDto();
        order.setOrderId(1);
        order.setPaymentMethod("카드");
        List<AdminOrderResponseDto> orders = List.of(order);
        when(orderMapper.findAll()).thenReturn(orders);

        assertThat(adminOrderService.findAll()).isSameAs(orders);
    }

    @Test
    void findByIdRejectsMissingOrder() {
        when(orderMapper.findById(99)).thenReturn(null);

        assertThatThrownBy(() -> adminOrderService.findById(99))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문입니다. 주문 ID: 99");
    }

    @Test
    void findDetailGroupsOptionsByOrderItem() {
        Order order = order(1, "접수");
        when(orderMapper.findById(1)).thenReturn(order);
        when(orderMapper.findItemsByOrderId(1)).thenReturn(List.of(
                row(10, "떡볶이", 1, 4000, 101, "치즈", 1000),
                row(10, "떡볶이", 1, 4000, 102, "계란", 500),
                row(11, "순대", 2, 3000, null, null, null)
        ));
        when(orderMapper.findSetComponentsByOrderId(1)).thenReturn(List.of(
                componentRow(10, 201, "떡볶이"),
                componentRow(10, 202, "순대")
        ));
        when(orderMapper.findSuccessfulPaymentMethod(1)).thenReturn("카카오페이");

        AdminOrderDetailResponseDto detail =
                adminOrderService.findDetailById(1);

        assertThat(detail.getOrderId()).isEqualTo(1);
        assertThat(detail.getPaymentMethod()).isEqualTo("카카오페이");
        assertThat(detail.getItems()).hasSize(2);
        assertThat(detail.getItems().get(0).getMenuName()).isEqualTo("떡볶이");
        assertThat(detail.getItems().get(0).getOptions())
                .extracting("optionName")
                .containsExactly("치즈", "계란");
        assertThat(detail.getItems().get(1).getMenuName()).isEqualTo("순대");
        assertThat(detail.getItems().get(1).getOptions()).isEmpty();
        assertThat(detail.getItems().get(0).getComponents())
                .extracting("componentMenuName")
                .containsExactly("떡볶이", "순대");
    }

    @Test
    void updateStatusAllowsOnlyNextStatusAndSavesHistory() {
        Order order = order(1, "접수");
        AdminOrderStatusRequestDto request = statusRequest(" 조리중 ");

        when(orderMapper.findById(1)).thenReturn(order);
        when(orderMapper.updateStatus(same(order))).thenReturn(1);
        when(currentAdminProvider.getAdminId()).thenReturn(7);

        int result = adminOrderService.updateStatus(1, request);

        assertThat(result).isEqualTo(1);
        assertThat(order.getOrderStatus()).isEqualTo("조리중");

        ArgumentCaptor<AdminHistory> historyCaptor =
                ArgumentCaptor.forClass(AdminHistory.class);
        verify(adminHistoryMapper).insertHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAdminId()).isEqualTo(7);
        assertThat(historyCaptor.getValue().getTitle()).isEqualTo("주문 상태 변경");
        assertThat(historyCaptor.getValue().getDescription())
                .contains("접수 → 조리중");
    }

    @Test
    void updateStatusRejectsSkippedTransition() {
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));

        assertThatThrownBy(
                () -> adminOrderService.updateStatus(1, statusRequest("완료"))
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("허용되지 않는 상태 전이입니다: 접수 → 완료");

        verify(orderMapper, never()).updateStatus(any(Order.class));
        verify(adminHistoryMapper, never()).insertHistory(any(AdminHistory.class));
    }

    @Test
    void updateStatusRejectsBlankStatus() {
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));

        assertThatThrownBy(
                () -> adminOrderService.updateStatus(1, statusRequest(" "))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("변경할 주문 상태를 입력해주세요.");

        verify(orderMapper, never()).updateStatus(any(Order.class));
    }

    @Test
    void cancelChangesActiveOrderAndSavesHistory() {
        when(orderMapper.findById(1)).thenReturn(order(1, "조리중"));
        when(orderMapper.cancel(1)).thenReturn(1);
        when(currentAdminProvider.getAdminId()).thenReturn(7);

        assertThat(adminOrderService.cancel(1)).isEqualTo(1);

        ArgumentCaptor<AdminHistory> historyCaptor =
                ArgumentCaptor.forClass(AdminHistory.class);
        verify(adminHistoryMapper).insertHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getAdminId()).isEqualTo(7);
        assertThat(historyCaptor.getValue().getTitle()).isEqualTo("주문 취소");
        assertThat(historyCaptor.getValue().getDescription())
                .contains("조리중 상태에서 취소");
    }

    @Test
    void cancelRejectsCompletedOrder() {
        when(orderMapper.findById(1)).thenReturn(order(1, "완료"));

        assertThatThrownBy(() -> adminOrderService.cancel(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("완료된 주문은 취소할 수 없습니다.");

        verify(orderMapper, never()).cancel(1);
        verify(adminHistoryMapper, never()).insertHistory(any(AdminHistory.class));
    }

    @Test
    void cancelRefundsTossPaymentBeforeCancelingOrder() {
        Payment payment = payment(11L, "카카오페이", "payment-key-1");
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));
        when(orderMapper.findSuccessfulPayment(1)).thenReturn(payment);
        when(orderMapper.markPaymentCanceled(11L)).thenReturn(1);
        when(orderMapper.cancel(1)).thenReturn(1);

        assertThat(adminOrderService.cancel(1)).isEqualTo(1);

        verify(tossPaymentService).cancel("payment-key-1", 1);
        verify(orderMapper).markPaymentCanceled(11L);
        verify(orderMapper).cancel(1);
    }

    @Test
    void cancelRejectsLegacyTossPaymentWithoutPaymentKey() {
        Payment payment = payment(11L, "토스페이", null);
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));
        when(orderMapper.findSuccessfulPayment(1)).thenReturn(payment);

        assertThatThrownBy(() -> adminOrderService.cancel(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("paymentKey가 없어 자동 환불할 수 없습니다");

        verify(tossPaymentService, never()).cancel(any(), any());
        verify(orderMapper, never()).markPaymentCanceled(any());
        verify(orderMapper, never()).cancel(1);
    }

    @Test
    void cancelKeepsDatabaseUnchangedWhenTossRefundFails() {
        Payment payment = payment(11L, "토스페이", "payment-key-1");
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));
        when(orderMapper.findSuccessfulPayment(1)).thenReturn(payment);
        doThrow(new IllegalStateException("결제 환불에 실패했습니다."))
                .when(tossPaymentService).cancel("payment-key-1", 1);

        assertThatThrownBy(() -> adminOrderService.cancel(1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 환불에 실패했습니다.");

        verify(orderMapper, never()).markPaymentCanceled(any());
        verify(orderMapper, never()).cancel(1);
        verify(adminHistoryMapper, never()).insertHistory(any(AdminHistory.class));
    }

    @Test
    void cancelSimulatedPaymentWithoutCallingRefundApi() {
        Payment payment = payment(12L, "네이버페이", null);
        when(orderMapper.findById(1)).thenReturn(order(1, "접수"));
        when(orderMapper.findSuccessfulPayment(1)).thenReturn(payment);
        when(orderMapper.cancel(1)).thenReturn(1);

        assertThat(adminOrderService.cancel(1)).isEqualTo(1);

        verifyNoInteractions(tossPaymentService);
        verify(orderMapper, never()).markPaymentCanceled(any());
        verify(orderMapper).cancel(1);
    }

    private Order order(Integer orderId, String status) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setOrderNumber("A-" + orderId);
        order.setOrderType("매장");
        order.setTotalPrice(10_000);
        order.setOrderStatus(status);
        order.setCreatedAt(LocalDateTime.of(2026, 7, 29, 10, 0));
        return order;
    }

    private Payment payment(Long paymentId, String paymentMethod, String paymentKey) {
        Payment payment = new Payment();
        payment.setPaymentId(paymentId);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentKey(paymentKey);
        payment.setPaymentStatus("성공");
        return payment;
    }

    private AdminOrderStatusRequestDto statusRequest(String status) {
        AdminOrderStatusRequestDto request = new AdminOrderStatusRequestDto();
        request.setOrderStatus(status);
        return request;
    }

    private AdminOrderItemRowDto row(
            Integer orderItemId,
            String menuName,
            Integer quantity,
            Integer unitPrice,
            Integer optionId,
            String optionName,
            Integer optionPrice
    ) {
        AdminOrderItemRowDto row = new AdminOrderItemRowDto();
        row.setOrderItemId(orderItemId);
        row.setMenuName(menuName);
        row.setQuantity(quantity);
        row.setUnitPrice(unitPrice);
        row.setOptionId(optionId);
        row.setOptionName(optionName);
        row.setOptionPrice(optionPrice);
        return row;
    }

    private AdminOrderSetComponentRowDto componentRow(
            Integer orderItemId,
            Integer componentMenuId,
            String componentMenuName
    ) {
        AdminOrderSetComponentRowDto row =
                new AdminOrderSetComponentRowDto();
        row.setOrderItemId(orderItemId);
        row.setComponentMenuId(componentMenuId);
        row.setComponentMenuName(componentMenuName);
        return row;
    }
}
