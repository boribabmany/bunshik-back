package com.bunshik.kiosk.controller;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import com.bunshik.common.config.SecurityConfig;
import com.bunshik.kiosk.dto.PaymentResponseDto;
import com.bunshik.kiosk.dto.TossConfirmRequestDto;
import com.bunshik.kiosk.dto.TossFailRequestDto;
import com.bunshik.kiosk.service.TossPaymentService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TossPaymentController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, AdminJwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class TossPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Boot 자동구성 ObjectMapper 빈에 의존하지 않고 테스트 자체 인스턴스를 사용
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TossPaymentService tossPaymentService;

    @Test
    void confirmReturnsSuccessResult() throws Exception {
        when(tossPaymentService.confirm(any()))
                .thenReturn(PaymentResponseDto.builder().status("성공").build());

        mockMvc.perform(post("/api/toss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("성공"));
    }

    @Test
    void confirmRejectsMissingPaymentKey() throws Exception {
        TossConfirmRequestDto request = confirmRequest();
        request.setPaymentKey("");

        mockMvc.perform(post("/api/toss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("paymentKey가 필요합니다."));

        verifyNoInteractions(tossPaymentService);
    }

    @Test
    void confirmPropagatesServiceErrorAsBadRequest() throws Exception {
        when(tossPaymentService.confirm(any()))
                .thenThrow(new IllegalArgumentException("결제 금액이 주문 금액과 일치하지 않습니다."));

        mockMvc.perform(post("/api/toss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(confirmRequest())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("결제 금액이 주문 금액과 일치하지 않습니다."));
    }

    @Test
    void failReturnsSuccessMessage() throws Exception {
        TossFailRequestDto request = new TossFailRequestDto();
        request.setOrderId(1);
        request.setMessage("사용자 취소");

        mockMvc.perform(post("/api/toss/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("결제가 취소되었습니다."));

        verify(tossPaymentService).fail(any());
    }

    @Test
    void failRejectsMissingOrderId() throws Exception {
        TossFailRequestDto request = new TossFailRequestDto();
        request.setMessage("사용자 취소");

        mockMvc.perform(post("/api/toss/fail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("주문 번호가 필요합니다."));

        verifyNoInteractions(tossPaymentService);
    }

    private TossConfirmRequestDto confirmRequest() {
        TossConfirmRequestDto request = new TossConfirmRequestDto();
        request.setOrderId(1);
        request.setPaymentKey("test-payment-key");
        request.setTossOrderId("toss-order-1");
        request.setAmount(10000);
        request.setPaymentMethod("토스페이");
        return request;
    }
}
