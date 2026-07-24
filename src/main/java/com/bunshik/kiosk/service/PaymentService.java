package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.PaymentRequestDto;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.mapper.OrderPaymentInfo;
import com.bunshik.kiosk.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {


    private static final Set<String> VALID_PAYMENT_METHODS = Set.of("카드", "네이버페이", "카카오페이");
    private static final Set<String> EASY_PAY_METHODS = Set.of("네이버페이", "카카오페이");

    private static final double CARD_FAILURE_RATE = 0.1;
    private static final double EASY_PAY_FAILURE_RATE = 0.1;

    private final PaymentMapper paymentMapper;
    private final Random random = new Random();

    public PaymentResponseDto processPayment(PaymentRequestDto request) {

        String paymentMethod = request.getPaymentMethod();

        if (!VALID_PAYMENT_METHODS.contains(paymentMethod)) {
            throw new IllegalArgumentException("지원하지 않는 결제 수단입니다: " + paymentMethod);
        }

        OrderPaymentInfo order = paymentMapper.getOrderForPayment(request.getOrderId());

        if (order == null) {
            throw new IllegalArgumentException("존재하지 않는 주문입니다: " + request.getOrderId());
        }

        if (paymentMapper.countSuccessfulPayments(order.getOrderId()) > 0) {
            throw new IllegalArgumentException("이미 결제가 완료된 주문입니다.");
        }

        PaymentResult result = simulatePayment(paymentMethod);

        paymentMapper.insertPayment(
                order.getOrderId(),
                order.getTotalPrice(),
                paymentMethod,
                result.success ? "성공" : "실패",
                result.failReason
        );

        if (result.success) {
            paymentMapper.updateOrderStatus(order.getOrderId(), "접수");
        }

        return PaymentResponseDto.builder()
                .status(result.success ? "성공" : "실패")
                .failReason(result.failReason)
                .build();
    }

    private PaymentResult simulatePayment(String paymentMethod) {

        if (paymentMethod.equals("카드")) {
            return random.nextDouble() < CARD_FAILURE_RATE
                    ? PaymentResult.fail("카드 승인이 거절되었습니다.")
                    : PaymentResult.success();
        }

        if (EASY_PAY_METHODS.contains(paymentMethod)) {
            return random.nextDouble() < EASY_PAY_FAILURE_RATE
                    ? PaymentResult.fail(paymentMethod + " 승인이 거절되었습니다.")
                    : PaymentResult.success();
        }

        return PaymentResult.success();
    }

    private static class PaymentResult {
        final boolean success;
        final String failReason;

        private PaymentResult(boolean success, String failReason) {
            this.success = success;
            this.failReason = failReason;
        }

        static PaymentResult success() {
            return new PaymentResult(true, null);
        }

        static PaymentResult fail(String reason) {
            return new PaymentResult(false, reason);
        }
    }
}