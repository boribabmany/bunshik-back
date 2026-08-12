package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.dto.TossConfirmRequestDto;
import com.bunshik.kiosk.dto.TossFailRequestDto;
import com.bunshik.kiosk.mapper.OrderPaymentInfo;
import com.bunshik.kiosk.mapper.PaymentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// 토스페이먼츠 실제 승인 API 호출(성공/HTTP 오류) 경로는 외부 네트워크 의존성이 있어
// 여기서는 API 호출 이전에 수행되는 주문 검증 로직만 단위 테스트로 검증한다.
@ExtendWith(MockitoExtension.class)
class TossPaymentServiceTest {

    @Mock
    private PaymentMapper paymentMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private TossPaymentService tossPaymentService;

    @Test
    void confirmRejectsNonExistentOrder() {
        when(paymentMapper.getOrderForPayment(99)).thenReturn(null);

        assertThatThrownBy(() -> tossPaymentService.confirm(confirmRequest(99, 10000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("존재하지 않는 주문입니다: 99");

        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
    }

    @Test
    void confirmReturnsSuccessForAlreadyPaidOrder() {
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(1);

        PaymentResponseDto response = tossPaymentService.confirm(confirmRequest(1, 10000));

        assertThat(response.getStatus()).isEqualTo("성공");
        // 이미 결제된 주문이므로 토스에 다시 승인 요청을 보내거나 결제를 중복 기록하면 안 됨
        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
        verify(paymentMapper, never()).updateOrderStatus(any(), any());
    }

    @Test
    void confirmRejectsOrderNotInPaymentPendingStatus() {
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "접수", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);

        assertThatThrownBy(() -> tossPaymentService.confirm(confirmRequest(1, 10000)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("결제 대기 중인 주문이 아닙니다: 접수");
    }

    @Test
    void confirmRejectsAmountMismatch() {
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);

        assertThatThrownBy(() -> tossPaymentService.confirm(confirmRequest(1, 9999)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("결제 금액이 주문 금액과 일치하지 않습니다.");

        verify(paymentMapper, never()).insertPayment(any(), any(), any(), any(), any(), any());
    }

    @Test
    void confirmStoresPaymentKeyReturnedByToss() {
        when(paymentMapper.getOrderForPayment(1)).thenReturn(orderInfo(1, "결제대기", 10000));
        when(paymentMapper.countSuccessfulPayments(1)).thenReturn(0);
        when(restTemplate.postForEntity(
                contains("/v1/payments/confirm"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("paymentKey", "confirmed-payment-key")));

        PaymentResponseDto response = tossPaymentService.confirm(confirmRequest(1, 10000));

        assertThat(response.getStatus()).isEqualTo("성공");
        verify(paymentMapper).insertPayment(
                1, 10000, "토스페이", "성공", null, "confirmed-payment-key"
        );
        verify(paymentMapper).updateOrderStatus(1, "접수");
    }

    @Test
    void failDoesNotThrowAndDoesNotTouchOrderState() {
        TossFailRequestDto request = new TossFailRequestDto();
        request.setOrderId(1);
        request.setMessage("사용자 취소");

        assertThatCode(() -> tossPaymentService.fail(request)).doesNotThrowAnyException();

        verifyNoInteractions(paymentMapper);
    }

    @Test
    void cancelCallsTossCancelApiWithPaymentKey() {
        ReflectionTestUtils.setField(tossPaymentService, "tossSecretKey", "test-secret");
        when(restTemplate.postForEntity(
                contains("/test-payment-key/cancel"),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(ResponseEntity.ok(Map.of("status", "CANCELED")));

        assertThatCode(() -> tossPaymentService.cancel("test-payment-key", 1))
                .doesNotThrowAnyException();

        verify(restTemplate).postForEntity(
                contains("/test-payment-key/cancel"),
                any(HttpEntity.class),
                eq(Map.class)
        );
    }

    private TossConfirmRequestDto confirmRequest(Integer orderId, Integer amount) {
        TossConfirmRequestDto request = new TossConfirmRequestDto();
        request.setOrderId(orderId);
        request.setPaymentKey("test-payment-key");
        request.setTossOrderId("toss-order-1");
        request.setAmount(amount);
        request.setPaymentMethod("토스페이");
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
