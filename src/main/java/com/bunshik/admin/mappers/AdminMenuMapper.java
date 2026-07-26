package com.bunshik.admin.mappers;

import com.bunshik.common.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMenuMapper {

    // 메뉴 전체 조회
    List<Menu> findAll();

    // 메뉴 한 개 조회
    Menu findById(
            @Param("menuId") Long menuId
    );

    // 메뉴 등록
    int insert(Menu menu);

    // 메뉴 수정
    int update(Menu menu);

    // 메뉴 삭제
    int delete(
            @Param("menuId") Long menuId
    );
}