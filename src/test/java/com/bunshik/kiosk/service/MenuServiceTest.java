package com.bunshik.kiosk.service;

import com.bunshik.kiosk.dto.MenuResponseDto;
import com.bunshik.kiosk.mapper.MenuMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuService menuService;

    @Test
    void getMenusReturnsMapperResult() {
        MenuResponseDto menu = new MenuResponseDto();
        menu.setMenuId(1L);
        menu.setMenuName("떡볶이");
        List<MenuResponseDto> menus = List.of(menu);

        when(menuMapper.findAll()).thenReturn(menus);

        assertThat(menuService.getMenus()).isSameAs(menus);
    }

    @Test
    void getMenusReturnsEmptyListWhenNoMenus() {
        when(menuMapper.findAll()).thenReturn(List.of());

        assertThat(menuService.getMenus()).isEmpty();
    }
}
