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

/**
 * 고객 주문 흐름 통합 테스트 — Controller → Service → MyBatis Mapper → 실제 로컬 MySQL 전체 스택을 검증한다.
 * (기존 *ControllerTest 들은 @WebMvcTest + Mockito로 서비스 계층을 목킹하는 슬라이스 테스트라 DB를 타지 않는다.)
 *
 * 전제:
 *  - bunshik-back/.env 의 DB_URL 이 로컬 MySQL(bunshik_db)을 가리키고 있어야 한다.
 *  - 메뉴 데이터가 최소 1개 이상 존재해야 하고, 그 중 옵션/세트 선택이 필요 없는
 *    판매중 메뉴가 하나는 있어야 한다 (kiosk-customer E2E의 "참치김밥"과 동일한 전제).
 *
 * 카드결제는 PaymentService.simulatePayment 에서 실제로 확률 처리되므로
 * (성공 90% / 승인거절 6% / 카드오류 2% / 지연 2%), kiosk-customer E2E와 동일하게
 * 최대 3회까지 재시도해서 flaky해지지 않도록 한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CustomerOrderFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createOrderAndPaySucceedsForPlainMenuItem() throws Exception {
        JsonNode plainMenu = findPlainAvailableMenu();

        int menuId = plainMenu.get("menu_id").asInt();

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
        assertThat(orderJson.get("data").get("status").asText()).isEqualTo("대기");

        int orderId = orderJson.get("data").get("order_id").asInt();

        String finalStatus = payWithRetry(orderId, 3);
        assertThat(finalStatus).isEqualTo("성공");
    }

    @Test
    void createOrderAndPaySucceedsForMenuWithOptions() throws Exception {
        JsonNode menu = findMenuWithOptions();
        int menuId = menu.get("menu_id").asInt();

        // 토핑은 선택(optional)이라 첫 번째로 판매 가능한 토핑 1개만 골라서 실제로 옵션 경로를 태운다
        JsonNode firstAvailableOption = null;
        for (JsonNode option : menu.get("options")) {
            if (option.get("option_is_available").asBoolean()) {
                firstAvailableOption = option;
                break;
            }
        }
        assertThat(firstAvailableOption)
                .as("판매 가능한 옵션이 하나도 없습니다: " + menu.get("menu_name").asText())
                .isNotNull();

        String orderBody = objectMapper.writeValueAsString(Map.of(
                "order_type", "매장",
                "items", List.of(Map.of(
                        "menu_id", menuId,
                        "quantity", 1,
                        "option_ids", List.of(firstAvailableOption.get("option_id").asInt()),
                        "component_menu_ids", List.of()
                ))
        ));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        assertThat(orderJson.get("success").asBoolean())
                .as("주문 생성 실패: " + orderResponse)
                .isTrue();

        int orderId = orderJson.get("data").get("order_id").asInt();

        assertThat(payWithRetry(orderId, 3)).isEqualTo("성공");
    }

    @Test
    void createOrderAndPaySucceedsForSetMenu() throws Exception {
        JsonNode menu = findMenuWithSetComponents();
        int menuId = menu.get("menu_id").asInt();

        List<Integer> componentMenuIds = pickOnePerGroup(menu.get("set_components"));

        String orderBody = objectMapper.writeValueAsString(Map.of(
                "order_type", "매장",
                "items", List.of(Map.of(
                        "menu_id", menuId,
                        "quantity", 1,
                        "option_ids", List.of(),
                        "component_menu_ids", componentMenuIds
                ))
        ));

        String orderResponse = mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode orderJson = objectMapper.readTree(orderResponse);
        assertThat(orderJson.get("success").asBoolean())
                .as("세트 메뉴(" + menu.get("menu_name").asText() + ") 주문 생성 실패: " + orderResponse)
                .isTrue();

        int orderId = orderJson.get("data").get("order_id").asInt();

        assertThat(payWithRetry(orderId, 3)).isEqualTo("성공");
    }

    @Test
    void tossConfirmFailsGracefullyForInvalidPaymentKey() throws Exception {
        // 실제 Toss 결제창을 거쳐야만 유효한 paymentKey가 발급되므로(브라우저 필요),
        // 여기서는 진짜 Toss 서버(https://api.tosspayments.com)에 가짜 paymentKey로 승인 요청을 보내
        // "정상적으로 실패 처리되는지"(에러가 그대로 터지지 않고 실패 응답 + 결제 실패 기록으로 이어지는지)를 검증한다.
        JsonNode plainMenu = findPlainAvailableMenu();
        int menuId = plainMenu.get("menu_id").asInt();
        int price = plainMenu.get("price").asInt();

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

        String tossBody = objectMapper.writeValueAsString(Map.of(
                "order_id", orderId,
                "payment_key", "e2e-invalid-payment-key-" + System.currentTimeMillis(),
                "toss_order_id", "bunshik-" + orderId + "-" + System.currentTimeMillis(),
                "amount", price,
                "payment_method", "토스페이"
        ));

        String tossResponse = mockMvc.perform(post("/api/toss/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(tossBody))
                .andReturn().getResponse().getContentAsString();

        JsonNode tossJson = objectMapper.readTree(tossResponse);
        assertThat(tossJson.get("success").asBoolean()).isFalse();
        assertThat(tossJson.get("message").asText()).contains("토스페이먼츠 승인에 실패");
    }

    @Test
    void cancelOrderTransitionsToCancelled() throws Exception {
        JsonNode plainMenu = findPlainAvailableMenu();
        int menuId = plainMenu.get("menu_id").asInt();

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

        String cancelResponse = mockMvc.perform(patch("/api/orders/{orderId}/cancel", orderId))
                .andReturn().getResponse().getContentAsString();

        JsonNode cancelJson = objectMapper.readTree(cancelResponse);
        assertThat(cancelJson.get("success").asBoolean()).isTrue();
        assertThat(cancelJson.get("message").asText()).isEqualTo("주문이 취소되었습니다.");
    }

    /** 옵션·세트 선택이 필요 없고 판매 중인 메뉴를 실제 DB 시드에서 동적으로 하나 찾는다. */
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

    /** 토핑(options)이 있고 세트 그룹은 없는, 판매 중인 메뉴를 동적으로 하나 찾는다. */
    private JsonNode findMenuWithOptions() throws Exception {
        String menusResponse = mockMvc.perform(get("/api/menus"))
                .andReturn().getResponse().getContentAsString();

        JsonNode menus = objectMapper.readTree(menusResponse).get("data");

        for (JsonNode menu : menus) {
            boolean available = menu.get("is_available").asBoolean();
            boolean hasOptions = menu.has("options") && menu.get("options").size() > 0;
            boolean hasSetComponents = menu.has("set_components") && menu.get("set_components").size() > 0;

            if (available && hasOptions && !hasSetComponents) {
                return menu;
            }
        }

        throw new AssertionError(
                "토핑(옵션)이 있는 판매중 메뉴를 찾지 못했습니다 (예: 라면/떡볶이). 로컬 DB 시드를 확인하세요."
        );
    }

    /** 그룹 선택(set_components)이 필요한, 판매 중인 세트 메뉴를 동적으로 하나 찾는다. */
    private JsonNode findMenuWithSetComponents() throws Exception {
        String menusResponse = mockMvc.perform(get("/api/menus"))
                .andReturn().getResponse().getContentAsString();

        JsonNode menus = objectMapper.readTree(menusResponse).get("data");

        for (JsonNode menu : menus) {
            boolean available = menu.get("is_available").asBoolean();
            boolean hasSetComponents = menu.has("set_components") && menu.get("set_components").size() > 0;

            if (available && hasSetComponents) {
                return menu;
            }
        }

        throw new AssertionError(
                "세트 구성이 필요한 판매중 메뉴를 찾지 못했습니다 (예: 김밥음료세트). 로컬 DB 시드를 확인하세요."
        );
    }

    /**
     * select_group별로 group_max_select개씩 판매 가능한 후보를 골라 component_menu_id 목록을 만든다.
     * (OrderService.validateAndCalculateSetComponents가 그룹당 정확히 group_max_select개를 요구함)
     */
    private List<Integer> pickOnePerGroup(JsonNode setComponents) {
        Map<String, List<JsonNode>> byGroup = new java.util.LinkedHashMap<>();

        for (JsonNode component : setComponents) {
            String group = component.get("select_group").isNull()
                    ? null
                    : component.get("select_group").asText();
            if (group == null) continue; // 고정 구성품 — 선택 불필요, 서버가 자동 포함
            byGroup.computeIfAbsent(group, k -> new ArrayList<>()).add(component);
        }

        List<Integer> picked = new ArrayList<>();

        for (Map.Entry<String, List<JsonNode>> entry : byGroup.entrySet()) {
            int maxSelect = entry.getValue().get(0).get("group_max_select").asInt();

            List<Integer> available = new ArrayList<>();
            for (JsonNode candidate : entry.getValue()) {
                if (candidate.get("is_available").asBoolean()) {
                    available.add(candidate.get("component_menu_id").asInt());
                }
                if (available.size() == maxSelect) break;
            }

            assertThat(available)
                    .as("그룹 '" + entry.getKey() + "'에서 " + maxSelect + "개를 고를 만큼 판매 가능한 후보가 없습니다.")
                    .hasSize(maxSelect);

            picked.addAll(available);
        }

        return picked;
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
}
