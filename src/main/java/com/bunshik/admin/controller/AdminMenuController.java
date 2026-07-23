package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.service.AdminMenuService;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    // 메뉴 전체 조회
    @GetMapping
    public List<Menu> findAll() {
        return adminMenuService.findAll();
    }

    // 메뉴 한 개 조회
    @GetMapping("/{menuId}")
    public Menu findById(@PathVariable Long menuId) {
        return adminMenuService.findById(menuId);
    }

    // 메뉴 등록
    @PostMapping
    public int insert(@RequestBody AdminMenuRequestDto dto) {
        return adminMenuService.insert(dto);
    }

    // 메뉴 수정
    @PutMapping("/{menuId}")
    public int update(@PathVariable Long menuId,
                      @RequestBody AdminMenuRequestDto dto) {

        return adminMenuService.update(menuId, dto);
    }

    // 메뉴 삭제
    @DeleteMapping("/{menuId}")
    public int delete(@PathVariable Long menuId) {
        return adminMenuService.delete(menuId);
    }
}