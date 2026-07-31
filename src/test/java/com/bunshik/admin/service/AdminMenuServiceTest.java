package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.admin.security.CurrentAdminProvider;
import com.bunshik.common.entity.Menu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminMenuServiceTest {

    @Mock
    private AdminMenuMapper menuMapper;

    @Mock
    private AdminHistoryMapper adminHistoryMapper;

    @Mock
    private CurrentAdminProvider currentAdminProvider;

    @InjectMocks
    private AdminMenuService adminMenuService;

    @Test
    void insertSetMenuSavesComponentsWithGeneratedMenuId() {
        AdminMenuRequestDto request = request("세트", List.of(1L, 2L));
        when(menuMapper.findAll()).thenReturn(List.of(
                menu(1L, "떡볶이"),
                menu(2L, "사이드")
        ));
        when(menuMapper.insert(any(Menu.class))).thenAnswer(invocation -> {
            Menu menu = invocation.getArgument(0);
            menu.setMenuId(10L);
            return 1;
        });

        Long menuId = adminMenuService.insert(request, null);

        assertThat(menuId).isEqualTo(10L);
        verify(menuMapper).deleteSetComponents(10L);
        verify(menuMapper).insertSetComponents(10L, List.of(1L, 2L));
    }

    @Test
    void insertSetMenuRejectsAnotherSetAsComponent() {
        AdminMenuRequestDto request = request("세트", List.of(2L));
        when(menuMapper.findAll()).thenReturn(List.of(menu(2L, "세트")));

        assertThatThrownBy(() -> adminMenuService.insert(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("다른 세트 메뉴는 구성 메뉴로 등록할 수 없습니다.");

        verify(menuMapper, never()).insert(any(Menu.class));
    }

    @Test
    void insertSetMenuRejectsEmptyComponents() {
        AdminMenuRequestDto request = request("세트", List.of());

        assertThatThrownBy(() -> adminMenuService.insert(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("세트 메뉴는 구성 메뉴를 한 개 이상 선택해야 합니다.");

        verify(menuMapper, never()).insert(any(Menu.class));
    }

    @Test
    void updateSetComponentsRemovesDuplicateIds() {
        Menu setMenu = menu(10L, "세트");
        setMenu.setMenuName("떡순튀 세트");
        when(menuMapper.findById(10L)).thenReturn(setMenu);
        when(menuMapper.findAll()).thenReturn(List.of(
                setMenu,
                menu(1L, "떡볶이"),
                menu(2L, "사이드")
        ));

        int count = adminMenuService.updateSetComponents(
                10L,
                List.of(1L, 1L, 2L)
        );

        assertThat(count).isEqualTo(2);
        verify(menuMapper).deleteSetComponents(10L);
        verify(menuMapper).insertSetComponents(10L, List.of(1L, 2L));
    }

    @Test
    void updateOrdinaryMenuRemovesPreviousSetRelations() {
        Menu oldMenu = menu(10L, "세트");
        oldMenu.setImageUrl("/old.png");
        AdminMenuRequestDto request = request("김밥", List.of());
        when(menuMapper.findById(10L)).thenReturn(oldMenu);
        when(menuMapper.update(any(Menu.class))).thenReturn(1);

        int result = adminMenuService.update(10L, request, null);

        assertThat(result).isEqualTo(1);
        verify(menuMapper).deleteSetComponents(10L);
        verify(menuMapper, never()).insertSetComponents(eq(10L), any());
    }

    @Test
    void insertRejectsUnknownCategory() {
        AdminMenuRequestDto request = request("분식", List.of());

        assertThatThrownBy(() -> adminMenuService.insert(request, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("올바른 메뉴 카테고리를 선택해주세요.");
    }

    private AdminMenuRequestDto request(
            String category,
            List<Long> componentMenuIds
    ) {
        AdminMenuRequestDto request = new AdminMenuRequestDto();
        request.setMenuName("테스트 메뉴");
        request.setMenuNameEn("Test menu");
        request.setPrice(10000);
        request.setCategory(category);
        request.setDescription("설명");
        request.setDescriptionEn("Description");
        request.setIsAvailable(true);
        request.setComponentMenuIds(componentMenuIds);
        return request;
    }

    private Menu menu(Long menuId, String category) {
        Menu menu = new Menu();
        menu.setMenuId(menuId);
        menu.setMenuName("메뉴 " + menuId);
        menu.setCategory(category);
        menu.setIsAvailable(true);
        menu.setEffectiveAvailable(true);
        menu.setIsVisible(true);
        return menu;
    }
}
