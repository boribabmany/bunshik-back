package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminSalesSummaryResponse;
import com.bunshik.admin.dto.PopularMenuResponse;
import com.bunshik.admin.dto.SalesHistoryResponse;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface AdminSalesMapper {

    AdminSalesSummaryResponse getSalesSummary();

    List<PopularMenuResponse> getPopularMenus();

    List<SalesHistoryResponse> getSalesHistory();

}