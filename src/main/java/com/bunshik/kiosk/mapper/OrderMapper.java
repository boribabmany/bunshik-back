package com.bunshik.kiosk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {

    // 주문 저장
    void insertOrder(@Param("orderType") String orderType,
                     @Param("totalPrice") Integer totalPrice);

    // 마지막 생성된 주문 PK
    Integer getLastOrderId();

    // 주문번호 수정
    void updateOrderNumber(@Param("orderId") Integer orderId,
                           @Param("orderNumber") String orderNumber);

    // 주문상품 저장
    void insertOrderItem(@Param("orderId") Integer orderId,
                         @Param("menuId") Integer menuId,
                         @Param("quantity") Integer quantity,
                         @Param("price") Integer price);

    // 마지막 주문상품 PK
    Integer getLastOrderItemId();

    // 주문상품 옵션 저장
    void insertOrderItemOption(@Param("orderItemId") Integer orderItemId,
                               @Param("optionId") Integer optionId);

    // 결제 저장
    void insertPayment(@Param("orderId") Integer orderId,
                       @Param("amount") Integer amount,
                       @Param("paymentMethod") String paymentMethod,
                       @Param("paymentStatus") String paymentStatus,
                       @Param("failReason") String failReason);

    // 메뉴 가격
    Integer getMenuPrice(Integer menuId);

    // 옵션 가격
    Integer getOptionPrice(Integer optionId);
}