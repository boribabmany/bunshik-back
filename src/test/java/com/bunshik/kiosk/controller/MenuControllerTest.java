package com.bunshik.kiosk.controller;

import com.bunshik.admin.jwt.AdminJwtAuthenticationFilter;
import com.bunshik.common.config.SecurityConfig;
import com.bunshik.kiosk.dto.MenuResponseDto;
import com.bunshik.kiosk.service.MenuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// SecurityConfig/JWT 필터는 관리자(admin) 전용 인증 로직이라 키오스크(고객용) API 테스트에서는
// 컨텍스트에서 제외하고, addFilters=false로 실제 필터 체인도 적용하지 않는다.
@WebMvcTest(
        controllers = MenuController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {SecurityConfig.class, AdminJwtAuthenticationFilter.class}
        )
)
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @Test
    void getMenusReturnsMenuList() throws Exception {
        MenuResponseDto menu = new MenuResponseDto();
        menu.setMenuId(1L);
        menu.setMenuName("떡볶이");
        menu.setPrice(4000);
        menu.setIsAvailable(true);

        when(menuService.getMenus()).thenReturn(List.of(menu));

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].menu_name").value("떡볶이"))
                .andExpect(jsonPath("$.data[0].price").value(4000));
    }

    @Test
    void getMenusReturnsEmptyListWhenNoMenus() throws Exception {
        when(menuService.getMenus()).thenReturn(List.of());

        mockMvc.perform(get("/api/menus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
    }
}
