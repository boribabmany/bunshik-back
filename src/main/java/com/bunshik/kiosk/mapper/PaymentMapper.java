package com.bunshik.kiosk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PaymentMapper {

    OrderPaymentInfo getOrderForPayment(@Param("orderId") Integer orderId);

    int countSuccessfulPayments(@Param("orderId") Integer orderId);

    void insertPayment(@Param("orderId") Integer orderId,
                       @Param("amount") Integer amount,
                       @Param("paymentMethod") String paymentMethod,
                       @Param("paymentStatus") String paymentStatus,
                       @Param("failReason") String failReason);

    void updateOrderStatus(@Param("orderId") Integer orderId,
                           @Param("orderStatus") String orderStatus);
}