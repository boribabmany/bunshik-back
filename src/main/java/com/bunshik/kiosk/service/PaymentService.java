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

        // 결제 성공 → 접수(주방으로 전달), 실패 → 취소(관리자 화면에 노출 안 됨)
        paymentMapper.updateOrderStatus(order.getOrderId(), result.success ? "접수" : "취소");

        return PaymentResponseDto.builder()
                .status(result.success ? "성공" : "실패")
                .failType(result.failType)
                .failReason(result.failReason)
                .build();
    }

    private PaymentResult simulatePayment(String paymentMethod) {

        if (paymentMethod.equals("카드")) {
            double roll = random.nextDouble();

            if (roll < 0.06) {
                return PaymentResult.fail("declined", "카드 승인이 거절되었습니다.");
            }
            if (roll < 0.08) {
                return PaymentResult.fail("card-error", "카드 정보를 확인할 수 없습니다.");
            }
            if (roll < 0.10) {
                return PaymentResult.fail("timeout", "결제 승인 응답이 지연되고 있습니다.");
            }
            return PaymentResult.success();
        }

        if (EASY_PAY_METHODS.contains(paymentMethod)) {
            double roll = random.nextDouble();

            if (roll < 0.08) {
                return PaymentResult.fail("declined", paymentMethod + " 승인이 거절되었습니다.");
            }
            if (roll < 0.10) {
                return PaymentResult.fail("timeout", paymentMethod + " 응답이 지연되고 있습니다.");
            }
            return PaymentResult.success();
        }

        return PaymentResult.success();
    }

    private static class PaymentResult {
        final boolean success;
        final String failType;
        final String failReason;

        private PaymentResult(boolean success, String failType, String failReason) {
            this.success = success;
            this.failType = failType;
            this.failReason = failReason;
        }

        static PaymentResult success() {
            return new PaymentResult(true, null, null);
        }

        static PaymentResult fail(String failType, String reason) {
            return new PaymentResult(false, failType, reason);
        }
    }
}