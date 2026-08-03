package com.bunshik.admin.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdminSalesMapperSqlTest {

    private static final Path MAPPER_XML = Path.of(
            "src/main/resources/mappers/admin/AdminSalesMapper.xml"
    );

    @Test
    void everySalesQueryUsesCompletedOrdersOnly() throws Exception {
        String sql = Files.readString(MAPPER_XML);

        assertThat(sql).doesNotContain("order_status NOT IN");
        assertThat(countOccurrences(
                sql,
                "order_status IN ('완료','COMPLETED')"
        )).isEqualTo(7);
    }

    @Test
    void dateAggregationUsesKoreaTime() throws Exception {
        String sql = Files.readString(MAPPER_XML);

        assertThat(sql)
                .contains("CONVERT_TZ(o.created_at, @@session.time_zone, '+09:00')")
                .contains("CONVERT_TZ(UTC_TIMESTAMP(), '+00:00', '+09:00')");
    }

    @Test
    void analyticsQueriesUseSelectedRangeAndSuccessfulPayments() throws Exception {
        String sql = Files.readString(MAPPER_XML);

        assertThat(sql)
                .contains(">= #{startDate}")
                .contains("&lt; #{endDateExclusive}")
                .contains("p.payment_status = '성공'");
    }

    private int countOccurrences(String value, String target) {
        return (value.length() - value.replace(target, "").length())
                / target.length();
    }
}
