package com.bunshik.kiosk.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrintJobMapper {

    void insertPrintJob(@Param("orderId") Integer orderId,
                        @Param("type") String type);

    PrintJobInfo findPendingPrintJob();

    List<PrintItemInfo> findItemsByOrderId(@Param("orderId") Integer orderId);

    void completePrintJob(@Param("printJobId") Long printJobId,
                          @Param("result") String result);
}