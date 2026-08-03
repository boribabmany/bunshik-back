package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminSalesSummaryResponse;
import com.bunshik.admin.dto.SalesAnalyticsResponse;
import com.bunshik.admin.dto.PopularMenuResponse;
import com.bunshik.admin.dto.SalesHistoryResponse;
import com.bunshik.admin.mappers.AdminSalesMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminSalesService {

    private final AdminSalesMapper adminSalesMapper;

    public AdminSalesSummaryResponse getSalesSummary() {
        return adminSalesMapper.getSalesSummary();
    }

    public List<PopularMenuResponse> getPopularMenus() {
        return adminSalesMapper.getPopularMenus();
    }

    public List<SalesHistoryResponse> getSalesHistory() {
        return adminSalesMapper.getSalesHistory();
    }

    public SalesAnalyticsResponse getSalesAnalytics(String period, LocalDate date) {
        String normalizedPeriod = period == null
                ? "day"
                : period.trim().toLowerCase(Locale.ROOT);
        LocalDate baseDate = date == null
                ? LocalDate.now(ZoneId.of("Asia/Seoul"))
                : date;

        LocalDate startDate;
        LocalDate endDateExclusive;

        switch (normalizedPeriod) {
            case "day" -> {
                startDate = baseDate;
                endDateExclusive = baseDate.plusDays(1);
            }
            case "week" -> {
                startDate = baseDate.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                );
                endDateExclusive = startDate.plusWeeks(1);
            }
            case "month" -> {
                startDate = baseDate.withDayOfMonth(1);
                endDateExclusive = startDate.plusMonths(1);
            }
            default -> throw new IllegalArgumentException(
                    "조회 기간은 day, week, month 중 하나여야 합니다."
            );
        }

        return new SalesAnalyticsResponse(
                normalizedPeriod,
                startDate,
                endDateExclusive.minusDays(1),
                adminSalesMapper.getPeriodSalesSummary(startDate, endDateExclusive),
                adminSalesMapper.getPeriodSalesHistory(startDate, endDateExclusive),
                adminSalesMapper.getMenuSales(startDate, endDateExclusive),
                adminSalesMapper.getPaymentMethodSales(startDate, endDateExclusive)
        );
    }
}
