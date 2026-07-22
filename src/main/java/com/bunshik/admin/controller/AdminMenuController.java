package com.bunshik.admin.controller;

import com.bunshik.admin.service.AdminMenuService;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    @GetMapping("/admin/menus")
    public List<Menu> findAll() {
        return adminMenuService.findAll();
    }
}