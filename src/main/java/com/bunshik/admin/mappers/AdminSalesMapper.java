package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminSalesSummaryResponse;
import com.bunshik.admin.dto.MenuSalesResponse;
import com.bunshik.admin.dto.PaymentMethodSalesResponse;
import com.bunshik.admin.dto.PeriodSalesSummaryResponse;
import com.bunshik.admin.dto.PopularMenuResponse;
import com.bunshik.admin.dto.SalesHistoryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
@Mapper
public interface AdminSalesMapper {

    AdminSalesSummaryResponse getSalesSummary();

    List<PopularMenuResponse> getPopularMenus();

    List<SalesHistoryResponse> getSalesHistory();

    PeriodSalesSummaryResponse getPeriodSalesSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDateExclusive") LocalDate endDateExclusive
    );

    List<SalesHistoryResponse> getPeriodSalesHistory(
            @Param("startDate") LocalDate startDate,
            @Param("endDateExclusive") LocalDate endDateExclusive
    );

    List<MenuSalesResponse> getMenuSales(
            @Param("startDate") LocalDate startDate,
            @Param("endDateExclusive") LocalDate endDateExclusive
    );

    List<PaymentMethodSalesResponse> getPaymentMethodSales(
            @Param("startDate") LocalDate startDate,
            @Param("endDateExclusive") LocalDate endDateExclusive
    );

}
