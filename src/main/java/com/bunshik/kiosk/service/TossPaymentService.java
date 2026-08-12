package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.dto.TossConfirmRequestDto;
import com.bunshik.kiosk.dto.TossFailRequestDto;
import com.bunshik.kiosk.mapper.OrderPaymentInfo;
import com.bunshik.kiosk.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TossPaymentService {

    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public PaymentResponseDto confirm(TossConfirmRequestDto request) {

        OrderPaymentInfo order = paymentMapper.getOrderForPayment(request.getOrderId());

        if (order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다: " + request.getOrderId());
        }
        if (paymentMapper.countSuccessfulPayments(order.getOrderId()) > 0) {
            // 이미 성공 처리된 주문 — 재시도 요청이므로 에러가 아니라 기존 성공 결과를 그대로 응답
            return PaymentResponseDto.builder().status("성공").build();
        }
        if (!"결제대기".equals(order.getOrderStatus())) {
            throw new IllegalStateException("결제 대기 중인 주문이 아닙니다: " + order.getOrderStatus());
        }
        if (!request.getAmount().equals(order.getTotalPrice())) {
            throw new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes()));

        Map<String, Object> body = Map.of(
                "paymentKey", request.getPaymentKey(),
                "orderId", request.getTossOrderId(),
                "amount", request.getAmount()
        );

        String confirmedPaymentKey;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            Object responsePaymentKey = response.getBody() == null
                    ? null
                    : response.getBody().get("paymentKey");

            if (!(responsePaymentKey instanceof String key) || key.isBlank()) {
                throw new IllegalStateException(
                        "Toss 승인 응답에서 paymentKey를 확인하지 못했습니다."
                );
            }

            confirmedPaymentKey = key;
        } catch (HttpClientErrorException e) {
            paymentMapper.insertPayment(order.getOrderId(), order.getTotalPrice(),
                    request.getPaymentMethod(), "실패", "토스 승인 실패: " + e.getStatusCode(),
                    request.getPaymentKey());
            throw new IllegalStateException("토스페이먼츠 승인에 실패했습니다.");
        }

        paymentMapper.insertPayment(order.getOrderId(), order.getTotalPrice(), request.getPaymentMethod(),
                "성공", null, confirmedPaymentKey);
        paymentMapper.updateOrderStatus(order.getOrderId(), "접수");

        return PaymentResponseDto.builder().status("성공").build();
    }

    public void fail(TossFailRequestDto request) {
        // 결제대기 상태 유지 → 손님이 키오스크에서 재시도 가능
    }

    public void cancel(String paymentKey, Integer orderId) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("결제 취소에 필요한 paymentKey가 없습니다.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization",
                "Basic " + Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes()));
        headers.set("Idempotency-Key", "bunshik-order-" + orderId + "-full-cancel");

        Map<String, Object> body = Map.of("cancelReason", "관리자 주문 취소");

        try {
            restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (RestClientResponseException e) {
            throw new IllegalStateException(
                    "결제 환불에 실패했습니다. Toss 응답 코드: " + e.getStatusCode(), e
            );
        } catch (RestClientException e) {
            throw new IllegalStateException(
                    "결제 환불 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.", e
            );
        }
    }
}
