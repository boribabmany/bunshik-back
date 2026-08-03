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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class TossPaymentService {

    private final PaymentMapper paymentMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public PaymentResponseDto confirm(TossConfirmRequestDto request) {

        OrderPaymentInfo order = paymentMapper.getOrderForPayment(request.getOrderId());

        if (order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다: " + request.getOrderId());
        }
        if (paymentMapper.countSuccessfulPayments(order.getOrderId()) > 0) {
            throw new IllegalArgumentException("이미 결제가 완료된 주문입니다.");
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

        try {
            restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    new HttpEntity<>(body, headers),
                    Map.class
            );
        } catch (HttpClientErrorException e) {
            paymentMapper.insertPayment(order.getOrderId(), order.getTotalPrice(),
                    "토스페이", "실패", "토스 승인 실패: " + e.getStatusCode());
            throw new IllegalStateException("토스페이먼츠 승인에 실패했습니다.");
        }

        paymentMapper.insertPayment(order.getOrderId(), order.getTotalPrice(), "토스페이", "성공", null);
        paymentMapper.updateOrderStatus(order.getOrderId(), "접수");

        return PaymentResponseDto.builder().status("성공").build();
    }

    public void fail(TossFailRequestDto request) {
        // 결제대기 상태 유지 → 손님이 키오스크에서 재시도 가능
    }
}