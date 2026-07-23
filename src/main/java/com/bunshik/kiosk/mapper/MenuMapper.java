package com.bunshik.kiosk.mapper;

import com.bunshik.common.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface MenuMapper {
    List<Menu> findAll();
}