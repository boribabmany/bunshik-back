package com.bunshik.kiosk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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

    int insertOrderItemSetComponents(
            @Param("orderItemId") Integer orderItemId,
            @Param("menuId") Integer menuId
    );

    void insertOrderItemOption(@Param("orderItemId") Integer orderItemId,
                               @Param("optionId") Integer optionId);

    List<SetGroupInfo> getSetGroupInfo(@Param("setMenuId") Integer setMenuId);

    void insertOrderItemSetComponent(@Param("orderItemId") Integer orderItemId,
                                     @Param("componentMenuId") Integer componentMenuId,
                                     @Param("componentMenuName") String componentMenuName);

    Integer getMenuPrice(Integer menuId);

    Integer getOptionPrice(Integer optionId);

    Boolean isMenuOrderable(Integer menuId);

    Boolean isOptionOrderable(Integer optionId);

    // 취소용: 현재 주문 상태 조회
    String getOrderStatus(@Param("orderId") Integer orderId);

    // 주문 취소 (상태를 '취소'로 변경)
    void cancelOrder(@Param("orderId") Integer orderId);
}
