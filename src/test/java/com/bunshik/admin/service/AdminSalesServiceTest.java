package com.bunshik.admin.service;

import com.bunshik.admin.dto.PeriodSalesSummaryResponse;
import com.bunshik.admin.dto.SalesAnalyticsResponse;
import com.bunshik.admin.mappers.AdminSalesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminSalesServiceTest {

    private AdminSalesService service;

    @BeforeEach
    void setUp() {
        AdminSalesMapper mapper = mock(AdminSalesMapper.class);
        when(mapper.getPeriodSalesSummary(any(), any()))
                .thenReturn(new PeriodSalesSummaryResponse());
        when(mapper.getPeriodSalesHistory(any(), any())).thenReturn(List.of());
        when(mapper.getMenuSales(any(), any())).thenReturn(List.of());
        when(mapper.getPaymentMethodSales(any(), any())).thenReturn(List.of());
        service = new AdminSalesService(mapper);
    }

    @ParameterizedTest
    @CsvSource({
            "day,2026-08-05,2026-08-05,2026-08-05",
            "week,2026-08-05,2026-08-03,2026-08-09",
            "month,2026-08-05,2026-08-01,2026-08-31"
    })
    void calculatesKoreaDateRange(
            String period,
            LocalDate baseDate,
            LocalDate expectedStart,
            LocalDate expectedEnd
    ) {
        SalesAnalyticsResponse result = service.getSalesAnalytics(period, baseDate);

        assertThat(result.getPeriod()).isEqualTo(period);
        assertThat(result.getStartDate()).isEqualTo(expectedStart);
        assertThat(result.getEndDate()).isEqualTo(expectedEnd);
    }

    @Test
    void rejectsUnsupportedPeriod() {
        assertThatThrownBy(
                () -> service.getSalesAnalytics("year", LocalDate.of(2026, 8, 5))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("day, week, month");
    }
}
