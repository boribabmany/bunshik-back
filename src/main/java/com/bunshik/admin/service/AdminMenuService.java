package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final AdminMenuMapper menuMapper;
    private final AdminHistoryMapper adminHistoryMapper;


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

        int result = menuMapper.insert(menu);


        if (result > 0) {
            saveHistory(
                    "메뉴 등록",
                    dto.getMenuName() + " 메뉴가 등록되었습니다."
            );
        }

        return result;
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

        int result = menuMapper.update(menu);


        if (result > 0) {
            saveHistory(
                    "메뉴 수정",
                    dto.getMenuName() + " 메뉴가 수정되었습니다."
            );
        }

        return result;
    }


    // 메뉴 삭제
    public int delete(Long menuId) {

        int result = menuMapper.delete(menuId);


        if (result > 0) {
            saveHistory(
                    "메뉴 삭제",
                    "메뉴(ID: " + menuId + ")가 삭제되었습니다."
            );
        }

        return result;
    }


    // 변경 내역 저장
    private void saveHistory(String title, String description) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}