package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.PrintItemDto;
import com.bunshik.kiosk.dto.PrintJobResponseDto;
import com.bunshik.kiosk.mapper.PrintItemInfo;
import com.bunshik.kiosk.mapper.PrintJobInfo;
import com.bunshik.kiosk.mapper.PrintJobMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PrintJobService {

    private final PrintJobMapper printJobMapper;

    // 손님이 OrderComplete 화면에서 버튼을 눌렀을 때 출력 작업 생성
    public void request(Integer orderId, String type) {
        printJobMapper.insertPrintJob(orderId, type);
    }

    // RTOS가 폴링하는 대기 중인 출력 작업 조회 (없으면 빈 리스트)
    public List<PrintJobResponseDto> pending() {

        PrintJobInfo job = printJobMapper.findPendingPrintJob();

        if (job == null) {
            return Collections.emptyList();
        }

        List<PrintItemDto> itemDtos = List.of();

        // 영수증 출력일 때만 품목 상세를 조회한다 (번호표만 찍을 땐 불필요)
        if ("RECEIPT".equals(job.getType())) {
            List<PrintItemInfo> items = printJobMapper.findItemsByOrderId(job.getOrderId());
            itemDtos = items.stream()
                    .map(item -> PrintItemDto.builder()
                            .menuName(item.getMenuName())
                            .quantity(item.getQuantity())
                            .price(item.getPriceAtOrder())
                            .build())
                    .toList();
        }

        return List.of(PrintJobResponseDto.builder()
                .id(job.getPrintJobId())
                .type(job.getType())
                .orderNumber(job.getOrderNumber())
                .orderType(job.getOrderType())
                .paymentMethod(job.getPaymentMethod())
                .items(itemDtos)
                .totalPrice(job.getTotalPrice())
                .status(job.getStatus())
                .build());
    }

    // RTOS가 영수증/번호표 출력을 마친 뒤 완료 신호
    public void complete(Long printJobId, String result) {
        printJobMapper.completePrintJob(printJobId, result);
    }
}