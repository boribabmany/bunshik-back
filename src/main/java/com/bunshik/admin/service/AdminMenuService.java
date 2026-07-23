package com.bunshik.admin.service;

import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final AdminMenuMapper adminMenuMapper;

    // 메뉴 전체 조회
    public List<Menu> findAll() {
        return adminMenuMapper.findAll();
    }

    // 메뉴 하나 조회
    public Menu findById(Long menuId) {
        return adminMenuMapper.findById(menuId);
    }

    // 메뉴 등록
    public int insert(Menu menu) {
        return adminMenuMapper.insert(menu);
    }

    // 메뉴 수정
    public int update(Menu menu) {
        return adminMenuMapper.update(menu);
    }

    // 메뉴 삭제
    public int delete(Long menuId) {
        return adminMenuMapper.delete(menuId);
    }
}