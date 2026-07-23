package com.bunshik.kiosk.mapper;

import com.bunshik.common.entity.Menu;
import com.bunshik.kiosk.dto.MenuResponseDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MenuMapper {
    List<MenuResponseDto> findAll();
}