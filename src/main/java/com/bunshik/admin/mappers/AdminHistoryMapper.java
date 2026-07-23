package com.bunshik.admin.mappers;

import com.bunshik.admin.dto.AdminHistoryResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminHistoryMapper {

    List<AdminHistoryResponseDto> getHistoryList();

}