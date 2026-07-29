package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.admin.security.CurrentAdminProvider;
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
    private final CurrentAdminProvider currentAdminProvider;

    // 실제 이미지 저장 경로
    private final String uploadPath = "uploads/menus/";

    // 메뉴 전체 조회
    public List<Menu> findAll() {
        return menuMapper.findAll();
    }

    // 메뉴 한 개 조회
    public Menu findById(Long menuId) {
        return menuMapper.findById(menuId);
    }

    // 메뉴 등록
    public int insert(AdminMenuRequestDto dto, MultipartFile file) {

        if (file != null && !file.isEmpty()) {
            String imageUrl = saveImage(file);
            dto.setImageUrl(imageUrl);
        }

        Menu menu = new Menu();

        menu.setMenuName(dto.getMenuName());
        menu.setMenuNameEn(dto.getMenuNameEn());
        menu.setPrice(dto.getPrice());
        menu.setCategory(dto.getCategory());
        menu.setImageUrl(dto.getImageUrl());
        menu.setDescription(dto.getDescription());
        menu.setDescriptionEn(dto.getDescriptionEn());
        menu.setIsAvailable(dto.getIsAvailable());

        // 새 메뉴는 기본적으로 화면에 표시
        menu.setIsVisible(true);

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

        Menu oldMenu = menuMapper.findById(menuId);

        if (oldMenu == null) {
            throw new RuntimeException("메뉴를 찾을 수 없습니다.");
        }

        boolean imageChanged = file != null && !file.isEmpty();

        if (imageChanged) {
            String imageUrl = saveImage(file);
            dto.setImageUrl(imageUrl);
        }

        Menu menu = new Menu();

        menu.setMenuId(menuId);
        menu.setMenuName(dto.getMenuName());
        menu.setMenuNameEn(dto.getMenuNameEn());
        menu.setPrice(dto.getPrice());
        menu.setCategory(dto.getCategory());

        // 새 이미지가 없으면 기존 이미지 유지
        if (dto.getImageUrl() != null && !dto.getImageUrl().isBlank()) {
            menu.setImageUrl(dto.getImageUrl());
        } else {
            menu.setImageUrl(oldMenu.getImageUrl());
        }

        menu.setDescription(dto.getDescription());
        menu.setDescriptionEn(dto.getDescriptionEn());
        menu.setIsAvailable(dto.getIsAvailable());
        menu.setSoldOutReason(dto.getSoldOutReason());

        int result = menuMapper.update(menu);

        if (result > 0) {
            // 새 이미지로 수정한 경우에만 기존 이미지 파일 삭제
            if (imageChanged) {
                deleteImage(oldMenu);
            }

            saveHistory(
                    "메뉴 수정",
                    dto.getMenuName() + " 메뉴가 수정되었습니다."
            );
        }

        return result;
    }

    // 판매중단: 논리 삭제
    public int stopSelling(Long menuId) {

        Menu menu = menuMapper.findById(menuId);

        if (menu == null) {
            throw new RuntimeException("메뉴를 찾을 수 없습니다.");
        }

        int result = menuMapper.stopSelling(menuId);

        if (result > 0) {
            saveHistory(
                    "메뉴 판매중단",
                    menu.getMenuName() + " 메뉴가 판매중단되었습니다."
            );
        }

        // 논리 삭제이므로 이미지 파일은 삭제하지 않음
        return result;
    }

    // 판매재개
    public int resumeSelling(Long menuId) {

        Menu menu = menuMapper.findById(menuId);

        if (menu == null) {
            throw new RuntimeException("메뉴를 찾을 수 없습니다.");
        }

        int result = menuMapper.resumeSelling(menuId);

        if (result > 0) {
            saveHistory(
                    "메뉴 판매재개",
                    menu.getMenuName() + " 메뉴가 판매재개되었습니다."
            );
        }

        return result;
    }

    // 이미지 저장
    private String saveImage(MultipartFile file) {

        try {
            String originalName = file.getOriginalFilename();

            if (originalName == null || originalName.isBlank()) {
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

            // DB에 저장되는 이미지 접근 경로
            return "/uploads/menus/" + saveName;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }

    // 기존 이미지 파일 삭제
    private void deleteImage(Menu menu) {

        if (menu == null
                || menu.getImageUrl() == null
                || menu.getImageUrl().isBlank()) {
            return;
        }

        try {
            String fileName = Paths.get(menu.getImageUrl())
                    .getFileName()
                    .toString();

            Path imagePath = Paths.get(uploadPath)
                    .resolve(fileName);

            Files.deleteIfExists(imagePath);

        } catch (IOException e) {
            throw new RuntimeException("메뉴 이미지 삭제 실패", e);
        }
    }

    // 변경 내역 저장
    private void saveHistory(String title, String description) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(currentAdminProvider.getAdminId());
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}