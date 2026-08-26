package com.bunshik.admin.service;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class AdminOrderMapperSqlTest {

    private static final Path MAPPER_XML = Path.of(
            "src/main/resources/mappers/admin/AdminOrderMapper.xml"
    );

    @Test
    void orderListExcludesOnlyPaymentPendingOrdersSoCanceledOrdersRemainSearchable()
            throws Exception {
        String sql = Files.readString(MAPPER_XML);

        assertThat(sql).contains("WHERE o.order_status != '결제대기'");
        assertThat(sql).doesNotContain("NOT IN ('결제대기', '취소')");
    }
}
