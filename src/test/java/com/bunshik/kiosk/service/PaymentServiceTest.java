package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.PaymentRequestDto;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.mapper.OrderPaymentInfo;
import com.bunshik.kiosk.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Test
    void processPaymentRejectsUnsupportedPaymentMethod() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        PaymentService paymentService = new PaymentService(paymentMapper);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest(1, "현금")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 결제 수단입니다: 현금");

        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
    }

    @Test
    void processPaymentRejectsNonExistentOrder() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        PaymentService paymentService = new PaymentService(paymentMapper);
        when(paymentMapper.getOrderForPayment(99)).thenReturn(null);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest(99, "카드")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문입니다: 99");
    }

    @Test
    void processPaymentReturnsSuccessForAlreadyPaidOrder() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        PaymentService paymentService = new PaymentService(paymentMapper);
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(1);

        PaymentResponseDto response = paymentService.processPayment(paymentRequest(1, "카드"));

        assertThat(response.getStatus()).isEqualTo("성공");
        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
        verify(paymentMapper, never()).updateOrderStatus(any(), any());
    }

    @Test
    void processPaymentRejectsOrderNotInPaymentPendingStatus() {
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        PaymentService paymentService = new PaymentService(paymentMapper);
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "접수", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest(1, "카드")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 대기 중인 주문이 아닙니다: 접수");
    }

    @Test
    void processPaymentWithCardProducesConsistentResultAcrossManyAttempts() {
        // 결제 성공/실패는 내부적으로 무작위로 결정되므로,
        // 여러 번 반복 실행하며 매 시도마다 응답과 매퍼 호출이 일관되는지 검증한다.
        for (int i = 0; i < 50; i++) {
            PaymentMapper paymentMapper = mock(PaymentMapper.class);
            PaymentService paymentService = new PaymentService(paymentMapper);
            when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 10000));
            when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);

            PaymentResponseDto response = paymentService.processPayment(paymentRequest(1, "카드"));

            assertThat(response.getStatus()).isIn("성공", "실패");

            if ("성공".equals(response.getStatus())) {
                assertThat(response.getFailType()).isNull();
                assertThat(response.getFailReason()).isNull();
                verify(paymentMapper).insertPayment(eq(1), eq(10000), eq("카드"), eq("성공"), eq(null), eq(null));
                verify(paymentMapper).updateOrderStatus(1, "접수");
            } else {
                assertThat(response.getFailType()).isNotNull();
                assertThat(response.getFailReason()).isNotNull();
                verify(paymentMapper).insertPayment(eq(1), eq(10000), eq("카드"), eq("실패"), eq(response.getFailReason()), eq(null));
                verify(paymentMapper).updateOrderStatus(1, "결제대기");
            }
        }
    }

    @Test
    void processPaymentWithEasyPayProducesConsistentResultAcrossManyAttempts() {
        for (int i = 0; i < 50; i++) {
            PaymentMapper paymentMapper = mock(PaymentMapper.class);
            PaymentService paymentService = new PaymentService(paymentMapper);
            when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 8000));
            when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);

            PaymentResponseDto response = paymentService.processPayment(paymentRequest(1, "네이버페이"));

            assertThat(response.getStatus()).isIn("성공", "실패");

            if ("성공".equals(response.getStatus())) {
                verify(paymentMapper).insertPayment(eq(1), eq(8000), eq("네이버페이"), eq("성공"), eq(null), eq(null));
                verify(paymentMapper).updateOrderStatus(1, "접수");
            } else {
                assertThat(response.getFailType()).isNotNull();
                assertThat(response.getFailReason()).contains("네이버페이");
                verify(paymentMapper).updateOrderStatus(1, "결제대기");
            }
        }
    }

    @Test
    void processPaymentRejectsKakaoPayNowRoutedThroughToss() {
        // 2026-08-06부터 카카오페이는 POST /api/toss/confirm으로 이관되어
        // 이 시뮬레이션 결제 API(/api/payments)에서는 더 이상 허용되지 않는다.
        PaymentMapper paymentMapper = mock(PaymentMapper.class);
        PaymentService paymentService = new PaymentService(paymentMapper);

        assertThatThrownBy(() -> paymentService.processPayment(paymentRequest(1, "카카오페이")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 결제 수단입니다: 카카오페이");

        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
    }

    private PaymentRequestDto paymentRequest(Integer orderId, String paymentMethod) {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setOrderId(orderId);
        request.setPaymentMethod(paymentMethod);
        return request;
    }

    private OrderPaymentInfo orderInfo(Integer orderId, String status, Integer totalPrice) {
        OrderPaymentInfo info = new OrderPaymentInfo();
        info.setOrderId(orderId);
        info.setOrderStatus(status);
        info.setTotalPrice(totalPrice);
        return info;
    }
}
