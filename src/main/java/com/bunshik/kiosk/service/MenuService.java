package com.bunshik.kiosk.service;

import com.bunshik.common.entity.Menu;
import com.bunshik.kiosk.dto.MenuResponseDto;
import com.bunshik.kiosk.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    public List<MenuResponseDto> getMenus() {
        return menuMapper.findAll();
    }
}