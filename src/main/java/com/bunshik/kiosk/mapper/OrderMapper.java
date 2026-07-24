package com.bunshik.kiosk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    void insertOrder(@Param("orderType") String orderType,
                     @Param("totalPrice") Integer totalPrice);

    Integer getLastOrderId();

    void updateOrderNumber(@Param("orderId") Integer orderId,
                           @Param("orderNumber") String orderNumber);

    void insertOrderItem(@Param("orderId") Integer orderId,
                         @Param("menuId") Integer menuId,
                         @Param("quantity") Integer quantity,
                         @Param("price") Integer price);

    Integer getLastOrderItemId();

    void insertOrderItemOption(@Param("orderItemId") Integer orderItemId,
                               @Param("optionId") Integer optionId);

    Integer getMenuPrice(Integer menuId);

    Integer getOptionPrice(Integer optionId);
}