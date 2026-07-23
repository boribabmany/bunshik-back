package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Order;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminOrderMapper {

    List<Order> findAll();

    Order findById(Integer orderId);

    int insert(Order order);

    int update(Order order);

    int delete(Integer orderId);
}