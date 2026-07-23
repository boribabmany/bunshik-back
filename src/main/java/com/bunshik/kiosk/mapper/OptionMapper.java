package com.bunshik.kiosk.mapper;

import com.bunshik.kiosk.dto.OptionResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OptionMapper {

    List<OptionResponseDto> findAll();
}