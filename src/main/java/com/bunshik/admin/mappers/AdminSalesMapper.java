package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminSalesResponse;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminSalesMapper {

    AdminSalesResponse getSalesSummary();

}