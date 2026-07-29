package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOrderDetailResponseDto;
import com.bunshik.admin.dto.AdminOrderItemResponseDto;
import com.bunshik.admin.dto.AdminOrderItemRowDto;
import com.bunshik.admin.dto.AdminOrderOptionResponseDto;
import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.admin.dto.AdminOrderStatusRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOrderMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminOrderService {

    private final AdminOrderMapper orderMapper;
    private final AdminHistoryMapper adminHistoryMapper;

    /*
     * 일반 상태 변경 API에서 허용되는 상태 순서
     *
     * 결제대기 → 접수 → 조리중 → 완료
     *
     * 취소는 updateStatus()가 아니라
     * cancel() 메서드에서 별도로 처리
     */
    private static final Map<String, List<String>> ALLOWED_TRANSITIONS =
            Map.of(
                    "결제대기", List.of("접수"),
                    "접수", List.of("조리중"),
                    "조리중", List.of("완료"),
                    "완료", List.of(),
                    "취소", List.of()
            );

    // 주문 전체 조회
    public List<Order> findAll() {
        return orderMapper.findAll();
    }

    // 주문 한 건 조회
    public Order findById(Integer orderId) {

        Order order = orderMapper.findById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 주문입니다. 주문 ID: " + orderId
            );
        }

        return order;
    }

    // 주문 상세 조회
    public AdminOrderDetailResponseDto findDetailById(Integer orderId) {

        // 주문 기본 정보 조회
        Order order = findById(orderId);

        // 주문 메뉴와 옵션 JOIN 결과 조회
        List<AdminOrderItemRowDto> rows =
                orderMapper.findItemsByOrderId(orderId);

        /*
         * 같은 주문 메뉴를 orderItemId 기준으로 묶기
         *
         * JOIN 결과는 옵션 개수만큼 같은 주문 메뉴가
         * 여러 줄로 조회될 수 있으므로 Map으로 합친다.
         */
        Map<Integer, AdminOrderItemResponseDto> itemMap =
                new LinkedHashMap<>();

        for (AdminOrderItemRowDto row : rows) {

            AdminOrderItemResponseDto item =
                    itemMap.get(row.getOrderItemId());

            // 아직 생성되지 않은 주문 메뉴이면 새로 생성
            if (item == null) {

                item = new AdminOrderItemResponseDto();

                item.setOrderItemId(row.getOrderItemId());
                item.setMenuName(row.getMenuName());
                item.setQuantity(row.getQuantity());
                item.setUnitPrice(row.getUnitPrice());
                item.setOptions(new ArrayList<>());

                itemMap.put(row.getOrderItemId(), item);
            }

            // 옵션이 있는 경우에만 추가
            if (row.getOptionId() != null) {

                AdminOrderOptionResponseDto option =
                        new AdminOrderOptionResponseDto();

                option.setOptionId(row.getOptionId());
                option.setOptionName(row.getOptionName());
                option.setOptionPrice(row.getOptionPrice());

                item.getOptions().add(option);
            }
        }

        // 최종 주문 상세 응답 생성
        AdminOrderDetailResponseDto detail =
                new AdminOrderDetailResponseDto();

        detail.setOrderId(order.getOrderId());
        detail.setOrderNumber(order.getOrderNumber());
        detail.setOrderType(order.getOrderType());
        detail.setTotalPrice(order.getTotalPrice());
        detail.setOrderStatus(order.getOrderStatus());
        detail.setCreatedAt(order.getCreatedAt());
        detail.setItems(new ArrayList<>(itemMap.values()));

        return detail;
    }

    // 주문 검색
    public List<Order> search(AdminOrderSearchRequestDto dto) {
        return orderMapper.search(dto);
    }

    // 주문 상태 변경
    @Transactional
    public int updateStatus(
            Integer orderId,
            AdminOrderStatusRequestDto dto
    ) {

        // 주문 존재 여부 확인
        Order order = findById(orderId);

        // 요청값 null, 공백 검사
        if (dto == null
                || dto.getOrderStatus() == null
                || dto.getOrderStatus().isBlank()) {

            throw new IllegalArgumentException(
                    "변경할 주문 상태를 입력해주세요."
            );
        }

        String currentStatus = order.getOrderStatus();
        String nextStatus = dto.getOrderStatus().trim();

        // 현재 DB 상태가 정상적인 상태인지 검사
        validateCurrentStatus(currentStatus);

        // 요청으로 들어온 다음 상태가 정상적인 상태인지 검사
        validateNextStatus(nextStatus);

        // 현재 상태와 다음 상태가 같은 경우 방지
        if (currentStatus.equals(nextStatus)) {
            throw new IllegalStateException(
                    "현재 주문 상태와 변경할 상태가 같습니다: "
                            + currentStatus
            );
        }

        // 상태 변경 순서 검사
        validateTransition(currentStatus, nextStatus);

        // 검증이 끝난 후 상태 변경
        order.setOrderStatus(nextStatus);

        int result = orderMapper.updateStatus(order);

        if (result == 0) {
            throw new IllegalStateException(
                    "주문 상태 변경에 실패했습니다."
            );
        }

        // 관리자 변경 내역 저장
        saveHistory(
                "주문 상태 변경",
                "주문번호 "
                        + order.getOrderNumber()
                        + " 상태가 "
                        + currentStatus
                        + " → "
                        + nextStatus
                        + "(으)로 변경되었습니다."
        );

        return result;
    }

    // 주문 취소
    @Transactional
    public int cancel(Integer orderId) {

        // 주문 존재 여부 확인
        Order order = findById(orderId);

        String currentStatus = order.getOrderStatus();

        // 현재 상태가 정상적인 상태인지 검사
        validateCurrentStatus(currentStatus);

        // 이미 취소된 주문 중복 취소 방지
        if ("취소".equals(currentStatus)) {
            throw new IllegalStateException(
                    "이미 취소된 주문입니다."
            );
        }

        // 완료된 주문 취소 방지
        if ("완료".equals(currentStatus)) {
            throw new IllegalStateException(
                    "완료된 주문은 취소할 수 없습니다."
            );
        }

        /*
         * 현재 정책:
         *
         * 결제대기 → 취소 가능
         * 접수     → 취소 가능
         * 조리중   → 취소 가능
         *
         * 완료, 취소 상태는 취소 불가능
         */

        int result = orderMapper.cancel(orderId);

        if (result == 0) {
            throw new IllegalStateException(
                    "주문 취소에 실패했습니다."
            );
        }

        // 관리자 변경 내역 저장
        saveHistory(
                "주문 취소",
                "주문번호 "
                        + order.getOrderNumber()
                        + "가 "
                        + currentStatus
                        + " 상태에서 취소되었습니다."
        );

        return result;
    }

    // 현재 DB에 저장된 상태 검사
    private void validateCurrentStatus(String currentStatus) {

        if (currentStatus == null
                || !ALLOWED_TRANSITIONS.containsKey(currentStatus)) {

            throw new IllegalStateException(
                    "알 수 없는 현재 주문 상태입니다: "
                            + currentStatus
            );
        }
    }

    // 요청으로 들어온 다음 상태 검사
    private void validateNextStatus(String nextStatus) {

        if (!ALLOWED_TRANSITIONS.containsKey(nextStatus)) {
            throw new IllegalArgumentException(
                    "잘못된 변경 상태입니다: "
                            + nextStatus
            );
        }

        /*
         * 취소는 상태 변경 API로 처리하지 않고
         * 별도의 cancel() 메서드로만 처리
         */
        if ("취소".equals(nextStatus)) {
            throw new IllegalStateException(
                    "주문 취소는 주문 취소 API를 사용해야 합니다."
            );
        }
    }

    // 상태 변경 순서 검사
    private void validateTransition(
            String currentStatus,
            String nextStatus
    ) {

        List<String> allowedNextStatuses =
                ALLOWED_TRANSITIONS.get(currentStatus);

        if (!allowedNextStatuses.contains(nextStatus)) {
            throw new IllegalStateException(
                    "허용되지 않는 상태 전이입니다: "
                            + currentStatus
                            + " → "
                            + nextStatus
            );
        }
    }

    // 관리자 변경 내역 저장
    private void saveHistory(
            String title,
            String description
    ) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}