package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminHistoryResponseDto;
import com.bunshik.common.entity.AdminHistory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminHistoryMapper {

    List<AdminHistoryResponseDto> getHistoryList();
    void insertHistory(AdminHistory history);
}