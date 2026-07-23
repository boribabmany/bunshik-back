package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final AdminMenuMapper menuMapper;

    // 메뉴 전체 조회
    public List<Menu> findAll() {
        return menuMapper.findAll();
    }

    // 메뉴 한 개 조회
    public Menu findById(Long menuId) {
        return menuMapper.findById(menuId);
    }

    // 메뉴 등록
    public int insert(AdminMenuRequestDto dto) {

        Menu menu = new Menu();
        menu.setMenuName(dto.getMenuName());
        menu.setPrice(dto.getPrice());
        menu.setCategory(dto.getCategory());
        menu.setImageUrl(dto.getImageUrl());
        menu.setDescription(dto.getDescription());
        menu.setIsAvailable(dto.getIsAvailable());
        menu.setSoldOutReason(dto.getSoldOutReason());

        return menuMapper.insert(menu);
    }

    // 메뉴 수정
    public int update(Long menuId, AdminMenuRequestDto dto) {

        Menu menu = new Menu();
        menu.setMenuId(menuId);
        menu.setMenuName(dto.getMenuName());
        menu.setPrice(dto.getPrice());
        menu.setCategory(dto.getCategory());
        menu.setImageUrl(dto.getImageUrl());
        menu.setDescription(dto.getDescription());
        menu.setIsAvailable(dto.getIsAvailable());
        menu.setSoldOutReason(dto.getSoldOutReason());

        return menuMapper.update(menu);
    }

    // 메뉴 삭제
    public int delete(Long menuId) {
        return menuMapper.delete(menuId);
    }
}