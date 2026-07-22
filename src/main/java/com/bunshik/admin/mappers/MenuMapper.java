package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenuMapper {

    List<Menu> findAll();

    Menu findById(Long menuId);

    int insert(Menu menu);

    int update(Menu menu);

    int delete(Long menuId);
}