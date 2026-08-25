package com.bunshik.kiosk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 주문 → 결제 → 출력 작업 등록 → RTOS 폴링 → 출력 완료까지 전체 흐름을 검증하는 통합 테스트.
 * CustomerOrderFlowIntegrationTest와 동일하게 Controller → Service → MyBatis Mapper →
 * 실제 로컬 MySQL 전체 스택을 검증하며(슬라이스 목킹 없음), RTOS Worker는 실제 프로세스를 띄우는 대신
 * 그 역할(GET /pending 폴링 → PATCH /complete 완료 신호)을 이 테스트가 그대로 흉내 낸다.
 *
 * 전제:
 *  - bunshik-back/.env 의 DB_URL 이 로컬 MySQL(bunshik_db)을 가리키고 있어야 한다.
 *  - 옵션/세트 선택이 필요 없는 판매중 메뉴가 최소 1개 존재해야 한다.
 *
 * 주의: GET /api/print-jobs/pending 은 가장 먼저 등록된 PENDING 작업 1건만 반환한다(FIFO).
 * 이전 테스트 실행에서 남은 미완료 작업이 있으면 그것부터 나오므로, 이 테스트는 자신의
 * order_number 가 나올 때까지 앞에 걸린 작업들을 대신 완료 처리하며(drain) 큐를 비운다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PrintJobFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void receiptPrintJobIsQueuedByRtosAndCompletedAfterPayment() throws Exception {
        JsonNode menu = findPlainAvailableMenu();
        int menuId = menu.get("menu_id").asInt();

        String orderBody = objectMapper.writeValueAsString(Map.of(
                "order_type", "매장",
                "items", List.of(Map.of(
                        "menu_id", menuId,
                        "quantity", 1,
                        "option_ids", List.of(),
                        "component_menu_ids", List.of()
                ))
        ));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        assertThat(orderJson.get("success").asBoolean()).isTrue();

        int orderId = orderJson.get("data").get("order_id").asInt();
        String orderNumber = orderJson.get("data").get("order_number").asText();

        assertThat(payWithRetry(orderId, 3)).isEqualTo("성공");

        // 손님이 OrderComplete 화면에서 "영수증 출력" 버튼을 누른 상황
        String printRequestBody = objectMapper.writeValueAsString(Map.of(
                "order_id", orderId,
                "type", "RECEIPT"
        ));

        String printRequestResponse = mockMvc.perform(post("/api/print-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(printRequestBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        JsonNode printRequestJson = objectMapper.readTree(printRequestResponse);
        assertThat(printRequestJson.get("success").asBoolean()).isTrue();
        assertThat(printRequestJson.get("message").asText()).isEqualTo("출력 요청이 접수되었습니다.");

        // RTOS Worker가 폴링해서 우리 작업을 집어들 때까지, 앞에 밀려있는 남의 작업은 대신 흘려보낸다(drain)
        JsonNode ourJob = pollUntilOwnJob(orderNumber, 20);

        assertThat(ourJob.get("type").asText()).isEqualTo("RECEIPT");
        assertThat(ourJob.get("order_type").asText()).isEqualTo("매장");
        assertThat(ourJob.get("payment_method").asText()).isEqualTo("카드");
        assertThat(ourJob.get("status").asText()).isEqualTo("PENDING");
        assertThat(ourJob.get("items")).isNotEmpty();
        assertThat(ourJob.get("items").get(0).get("menu_name").asText())
                .isEqualTo(menu.get("menu_name").asText());

        long printJobId = ourJob.get("id").asLong();

        // RTOS가 실제 프린터로 출력을 마친 뒤 완료 신호를 보내는 단계
        String completeBody = objectMapper.writeValueAsString(Map.of("result", "SUCCESS"));

        String completeResponse = mockMvc.perform(patch("/api/print-jobs/{id}/complete", printJobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode completeJson = objectMapper.readTree(completeResponse);
        assertThat(completeJson.get("success").asBoolean()).isTrue();
        assertThat(completeJson.get("message").asText()).isEqualTo("출력 완료 처리되었습니다.");

        // 완료된 작업은 더 이상 대기열에 나타나지 않아야 한다
        String pendingAfterComplete = mockMvc.perform(get("/api/print-jobs/pending"))
                .andReturn().getResponse().getContentAsString();
        JsonNode pendingAfterCompleteData = objectMapper.readTree(pendingAfterComplete).get("data");
        if (pendingAfterCompleteData.size() > 0) {
            assertThat(pendingAfterCompleteData.get(0).get("id").asLong()).isNotEqualTo(printJobId);
        }
    }

    @Test
    void orderNumberOnlyPrintJobCarriesNoItemPayload() throws Exception {
        JsonNode menu = findPlainAvailableMenu();
        int menuId = menu.get("menu_id").asInt();

        String orderBody = objectMapper.writeValueAsString(Map.of(
                "order_type", "포장",
                "items", List.of(Map.of(
                        "menu_id", menuId,
                        "quantity", 1,
                        "option_ids", List.of(),
                        "component_menu_ids", List.of()
                ))
        ));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        int orderId = orderJson.get("data").get("order_id").asInt();
        String orderNumber = orderJson.get("data").get("order_number").asText();

        assertThat(payWithRetry(orderId, 3)).isEqualTo("성공");

        String printRequestBody = objectMapper.writeValueAsString(Map.of(
                "order_id", orderId,
                "type", "ORDER_NUMBER"
        ));

        mockMvc.perform(post("/api/print-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(printRequestBody))
                .andExpect(status().isCreated());

        JsonNode ourJob = pollUntilOwnJob(orderNumber, 20);

        assertThat(ourJob.get("type").asText()).isEqualTo("ORDER_NUMBER");
        assertThat(ourJob.get("items")).isEmpty();

        long printJobId = ourJob.get("id").asLong();
        String completeBody = objectMapper.writeValueAsString(Map.of("result", "SUCCESS"));

        mockMvc.perform(patch("/api/print-jobs/{id}/complete", printJobId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(completeBody))
                .andExpect(status().isOk());
    }

    @Test
    void printJobRequestRejectsInvalidType() throws Exception {
        JsonNode menu = findPlainAvailableMenu();
        int menuId = menu.get("menu_id").asInt();

        String orderBody = objectMapper.writeValueAsString(Map.of(
                "order_type", "매장",
                "items", List.of(Map.of(
                        "menu_id", menuId,
                        "quantity", 1,
                        "option_ids", List.of(),
                        "component_menu_ids", List.of()
                ))
        ));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andReturn().getResponse().getContentAsString();

        int orderId = objectMapper.readTree(orderResponse).get("data").get("order_id").asInt();

        String invalidBody = objectMapper.writeValueAsString(Map.of(
                "order_id", orderId,
                "type", "PDF"
        ));

        mockMvc.perform(post("/api/print-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void printJobRequestRejectsMissingOrderId() throws Exception {
        String missingOrderIdBody = objectMapper.writeValueAsString(Map.of("type", "RECEIPT"));

        mockMvc.perform(post("/api/print-jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(missingOrderIdBody))
                .andExpect(status().isBadRequest());
    }

    /** 옵션/세트 선택이 필요 없고 판매 중인 메뉴를 실제 DB 시드에서 동적으로 하나 찾는다. */
    private JsonNode findPlainAvailableMenu() throws Exception {
        String menusResponse = mockMvc.perform(get("/api/menus"))
                .andReturn().getResponse().getContentAsString();

        JsonNode menus = objectMapper.readTree(menusResponse).get("data");
        List<String> names = new ArrayList<>();

        for (JsonNode menu : menus) {
            names.add(menu.get("menu_name").asText());
            boolean available = menu.get("is_available").asBoolean();
            boolean hasOptions = menu.has("options") && menu.get("options").size() > 0;
            boolean hasSetComponents = menu.has("set_components") && menu.get("set_components").size() > 0;

            if (available && !hasOptions && !hasSetComponents) {
                return menu;
            }
        }

        throw new AssertionError(
                "옵션/세트 선택이 필요 없는 판매중 메뉴를 찾지 못했습니다. " +
                        "로컬 DB의 menus 시드를 확인하세요. 조회된 메뉴: " + names
        );
    }

    /** 카드결제는 확률 기반이라 실패(재시도 가능) 시 최대 maxAttempts까지 반복 요청한다. */
    private String payWithRetry(int orderId, int maxAttempts) throws Exception {
        String lastStatus = null;

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String paymentBody = objectMapper.writeValueAsString(Map.of(
                    "order_id", orderId,
                    "payment_method", "카드"
            ));

            String paymentResponse = mockMvc.perform(post("/api/payments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(paymentBody))
                    .andReturn().getResponse().getContentAsString();

            JsonNode paymentJson = objectMapper.readTree(paymentResponse);
            assertThat(paymentJson.get("success").asBoolean()).isTrue();

            lastStatus = paymentJson.get("data").get("status").asText();
            if ("성공".equals(lastStatus)) {
                return lastStatus;
            }
        }

        return lastStatus;
    }

    /**
     * GET /pending 은 가장 오래된 PENDING 작업 1건만 돌려준다(FIFO, RTOS 실제 폴링 방식과 동일).
     * 우리 주문번호가 나올 때까지, 앞서 밀려있는 다른 작업은 이 테스트가 RTOS 대신 완료 처리해
     * 큐를 비운다. maxPolls 회 안에 우리 작업을 찾지 못하면 실패로 처리한다.
     */
    private JsonNode pollUntilOwnJob(String orderNumber, int maxPolls) throws Exception {
        for (int i = 0; i < maxPolls; i++) {
            String pendingResponse = mockMvc.perform(get("/api/print-jobs/pending"))
                    .andReturn().getResponse().getContentAsString();

            JsonNode pendingData = objectMapper.readTree(pendingResponse).get("data");

            assertThat(pendingData)
                    .as("출력 요청을 등록했는데 대기열이 비어 있습니다 (order_number=" + orderNumber + ")")
                    .isNotEmpty();

            JsonNode job = pendingData.get(0);
            if (orderNumber.equals(job.get("order_number").asText())) {
                return job;
            }

            // 남의(이전 실행에서 남은) 작업이면 RTOS 대신 흘려보내고 다음 것을 본다
            long strayJobId = job.get("id").asLong();
            String drainBody = objectMapper.writeValueAsString(Map.of("result", "DRAINED_BY_TEST"));
            mockMvc.perform(patch("/api/print-jobs/{id}/complete", strayJobId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(drainBody));
        }

        throw new AssertionError(
                "대기열에서 주문번호 " + orderNumber + "의 출력 작업을 " + maxPolls + "회 폴링 안에 찾지 못했습니다."
        );
    }
}
