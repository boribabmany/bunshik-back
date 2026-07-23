package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminOrderSearchRequestDto;
import com.bunshik.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminOrderMapper {

    // 주문 전체 조회
    List<Order> findAll();

    // 주문 상세 조회
    Order findById(Integer orderId);

    // 주문 검색 (날짜, 주문유형, 주문상태)
    List<Order> search(AdminOrderSearchRequestDto dto);

    // 주문 상태 변경
    int updateStatus(Order order);

    // 주문 취소
    int cancel(Integer orderId);

}