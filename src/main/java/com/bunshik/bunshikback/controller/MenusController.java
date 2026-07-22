package com.bunshik.bunshikback.controller;

import com.bunshik.bunshikback.entity.Menus;
import com.bunshik.bunshikback.service.MenusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
public class MenusController {

    private final MenusService menusService;

    public MenusController(MenusService menusService) {
        this.menusService = menusService;
    }
    // 전체 메뉴 조회
    @GetMapping
    public List<Menus> findAll() {
        return menusService.findAll();
    }
    // 메뉴 상세 조회
    @GetMapping("/{menuId}")
    public Menus findById(@PathVariable Long menuId) {
        return menusService.findById(menuId);
    }
    // 메뉴 등록
    @PostMapping
    public int insert(@RequestBody Menus menu) {
        return menusService.insert(menu);
    }
    // 메뉴 수정
    @PutMapping("/{menuId}")
    public int update(@PathVariable Long menuId,
                      @RequestBody Menus menu) {
        menu.setMenuId(menuId);
        return menusService.update(menu);
    }
    // 메뉴 삭제
    @DeleteMapping("/{menuId}")
    public int delete(@PathVariable Long menuId) {
        return menusService.delete(menuId);
    }
}