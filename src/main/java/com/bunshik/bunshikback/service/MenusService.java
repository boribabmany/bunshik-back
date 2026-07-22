package com.bunshik.bunshikback.service;

import com.bunshik.bunshikback.entity.Menus;
import com.bunshik.bunshikback.mapper.MenusMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenusService {

    private final MenusMapper menusMapper;

    public MenusService(MenusMapper menusMapper) {
        this.menusMapper = menusMapper;
    }
    // 전체 메뉴 조회
    public List<Menus> findAll() {
        return menusMapper.findAll();
    }
    // 메뉴 상세 조회
    public Menus findById(Long menuId) {
        return menusMapper.findById(menuId);
    }
    // 메뉴 등록
    public int insert(Menus menu) {
        return menusMapper.insert(menu);
    }
    // 메뉴 수정
    public int update(Menus menu) {
        return menusMapper.update(menu);
    }
    // 메뉴 삭제
    public int delete(Long menuId) {
        return menusMapper.delete(menuId);
    }
}