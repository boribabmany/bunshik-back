package com.bunshik.kiosk.controller;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import com.bunshik.common.config.SecurityConfig;
import com.bunshik.kiosk.dto.PaymentRequestDto;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.service.PaymentService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = PaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, AdminJwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Boot 자동구성 ObjectMapper 빈에 의존하지 않고 테스트 자체 인스턴스를 사용
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PaymentService paymentService;

    @Test
    void processPaymentReturnsSuccessResult() throws Exception {
        PaymentResponseDto response = PaymentResponseDto.builder()
                .status("성공")
                .build();

        when(paymentService.processPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(1, "카드"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("성공"));
    }

    @Test
    void processPaymentReturnsFailureResult() throws Exception {
        PaymentResponseDto response = PaymentResponseDto.builder()
                .status("실패")
                .failType("declined")
                .failReason("카드 승인이 거절되었습니다.")
                .build();

        when(paymentService.processPayment(any())).thenReturn(response);

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(1, "카드"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("실패"))
                .andExpect(jsonPath("$.data.fail_type").value("declined"));
    }

    @Test
    void processPaymentRejectsMissingOrderId() throws Exception {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setPaymentMethod("카드");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문 번호가 필요합니다."));

        verifyNoInteractions(paymentService);
    }

    @Test
    void processPaymentRejectsBlankPaymentMethod() throws Exception {
        PaymentRequestDto request = paymentRequest(1, "");

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 수단을 선택해주세요."));

        verifyNoInteractions(paymentService);
    }

    @Test
    void processPaymentPropagatesServiceErrorAsBadRequest() throws Exception {
        when(paymentService.processPayment(any()))
                .thenThrow(new IllegalArgumentException("존재하지 않는 주문입니다: 99"));

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(paymentRequest(99, "카드"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 주문입니다: 99"));
    }

    private PaymentRequestDto paymentRequest(Integer orderId, String paymentMethod) {
        PaymentRequestDto request = new PaymentRequestDto();
        request.setOrderId(orderId);
        request.setPaymentMethod(paymentMethod);
        return request;
    }
}
