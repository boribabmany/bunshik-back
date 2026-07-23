package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.common.entity.Menu;
import com.bunshik.kiosk.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ApiResponse<List<Menu>> getMenus() {
        return ApiResponse.success(menuService.getMenus());
    }
}