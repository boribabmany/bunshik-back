package com.bunshik.bunshikback.mapper;

import com.bunshik.bunshikback.entity.Menus;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MenusMapper {
    // 전체 메뉴 조회
    List<Menus> findAll();
    // 메뉴 상세 조회
    Menus findById(Long menuId);
    // 메뉴 등록
    int insert(Menus menu);
    // 메뉴 수정
    int update(Menus menu);
    // 메뉴 삭제
    int delete(Long menuId);
}