package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private final AdminMenuMapper menuMapper;
    private final AdminHistoryMapper adminHistoryMapper;

    // 이미지 저장 경로
    private final String uploadPath = "uploads/menus/";

    // 메뉴 전체 조회
    public List<Menu> findAll() {
        return menuMapper.findAll();
    }

    // 메뉴 한 개 조회
    public Menu findById(Long menuId) {
        return menuMapper.findById(menuId);
    }

    // 메뉴 등록 (파일 업로드 포함)
    public int insert(AdminMenuRequestDto dto, MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            try {

                String originalName = file.getOriginalFilename();
                if (originalName == null) {
                    originalName = "image";
                }

                String saveName = UUID.randomUUID() + "_" + originalName;

                Path uploadDir = Paths.get(uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path savePath = uploadDir.resolve(saveName);

                Files.copy(
                        file.getInputStream(),
                        savePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                dto.setImageUrl("/images/menus/" + saveName);

            } catch (IOException e) {
                throw new RuntimeException("이미지 저장 실패", e);
            }
        }

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
    public int update(
            Long menuId,
            AdminMenuRequestDto dto,
            MultipartFile file
    ) {

        // 새 이미지 저장
        if (file != null && !file.isEmpty()) {

            try {

                String originalName = file.getOriginalFilename();

                if (originalName == null) {
                    originalName = "image";
                }

                String saveName = UUID.randomUUID() + "_" + originalName;

                Path uploadDir = Paths.get(uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path savePath = uploadDir.resolve(saveName);

                Files.copy(
                        file.getInputStream(),
                        savePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                dto.setImageUrl("/images/menus/" + saveName);

            } catch (IOException e) {
                throw new RuntimeException("이미지 수정 저장 실패", e);
            }
        }

        Menu menu = new Menu();

        menu.setMenuId(menuId);
        menu.setMenuName(dto.getMenuName());
        menu.setPrice(dto.getPrice());
        menu.setCategory(dto.getCategory());

        // 이미지 없으면 기존 이미지 유지
        if (dto.getImageUrl() != null) {
            menu.setImageUrl(dto.getImageUrl());
        } else {
            Menu oldMenu = menuMapper.findById(menuId);
            if (oldMenu != null) {
                menu.setImageUrl(oldMenu.getImageUrl());
            }
        }

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

        Menu menu = menuMapper.findById(menuId);

        int result = menuMapper.delete(menuId);

        if (result > 0) {

            if (menu != null && menu.getImageUrl() != null) {

                try {

                    String fileName = Paths.get(menu.getImageUrl())
                            .getFileName()
                            .toString();

                    Path path = Paths.get(uploadPath)
                            .resolve(fileName);

                    Files.deleteIfExists(path);

                } catch (IOException e) {
                    throw new RuntimeException("이미지 삭제 실패", e);
                }
            }

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