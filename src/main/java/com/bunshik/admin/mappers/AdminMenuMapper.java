package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Menu;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMenuMapper {

    List<Menu> findAll();

    Menu findById(Long menuId);

    int insert(Menu menu);

    int update(Menu menu);

    // 논리 삭제
    int stopSelling(Long menuId);

    // 판매 재개
    int resumeSelling(Long menuId);
}