package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.dto.SetMenuComponentDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminMenuMapper;
import com.bunshik.admin.security.CurrentAdminProvider;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMenuService {

    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "세트", "떡볶이", "떡볶이맛", "라면", "김밥", "순대구성", "사이드", "음료"
    );
    private static final Set<String> ALLOWED_MENU_TYPES = Set.of(
            "NORMAL", "COMPONENT"
    );

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

    public List<SetMenuComponentDto> findSetComponents(Long menuId) {
        requireSetMenu(menuId);
        return menuMapper.findSetComponents(menuId);
    }

    @Transactional
    public int updateSetComponents(Long menuId, List<Long> componentMenuIds) {
        Menu setMenu = requireSetMenu(menuId);
        List<Long> uniqueComponentIds = validateComponentMenuIds(
                menuId,
                componentMenuIds
        );
        replaceSetComponents(menuId, fixedSettings(uniqueComponentIds));

        saveHistory(
                "세트 구성 변경",
                setMenu.getMenuName() + " 세트의 구성 메뉴가 변경되었습니다."
        );

        return uniqueComponentIds.size();
    }

    private Menu requireSetMenu(Long menuId) {
        Menu menu = menuMapper.findById(menuId);

        if (menu == null) {
            throw new IllegalArgumentException("메뉴를 찾을 수 없습니다.");
        }

        if (!"세트".equals(menu.getCategory())) {
            throw new IllegalArgumentException("세트 메뉴만 구성 메뉴를 관리할 수 있습니다.");
        }

        return menu;
    }

    private List<Long> validateComponentMenuIds(
            Long setMenuId,
            List<Long> componentMenuIds
    ) {
        List<Long> uniqueComponentIds = componentMenuIds == null
                ? List.of()
                : new LinkedHashSet<>(componentMenuIds).stream().toList();

        if (uniqueComponentIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "세트 메뉴는 구성 메뉴를 한 개 이상 선택해야 합니다."
            );
        }

        List<Menu> menus = menuMapper.findAll();

        for (Long componentMenuId : uniqueComponentIds) {
            if (componentMenuId == null || componentMenuId.equals(setMenuId)) {
                throw new IllegalArgumentException("세트 메뉴 자신은 구성 메뉴로 등록할 수 없습니다.");
            }

            Menu component = menus.stream()
                    .filter(menu -> componentMenuId.equals(menu.getMenuId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "존재하지 않는 구성 메뉴입니다: " + componentMenuId
                    ));

            if ("세트".equals(component.getCategory())) {
                throw new IllegalArgumentException("다른 세트 메뉴는 구성 메뉴로 등록할 수 없습니다.");
            }
        }

        return uniqueComponentIds;
    }

    private List<SetMenuComponentDto> fixedSettings(List<Long> componentMenuIds) {
        return componentMenuIds.stream().map(componentMenuId -> {
            SetMenuComponentDto setting = new SetMenuComponentDto();
            setting.setComponentMenuId(componentMenuId);
            setting.setExtraPrice(0);
            return setting;
        }).toList();
    }

    private List<SetMenuComponentDto> validateComponentSettings(
            List<Long> componentMenuIds,
            List<SetMenuComponentDto> settings
    ) {
        if (settings == null || settings.isEmpty()) {
            return fixedSettings(componentMenuIds);
        }

        Set<Long> settingIds = settings.stream()
                .map(SetMenuComponentDto::getComponentMenuId)
                .collect(Collectors.toSet());
        if (settingIds.size() != settings.size()
                || !settingIds.equals(new LinkedHashSet<>(componentMenuIds))) {
            throw new IllegalArgumentException("선택한 모든 구성 메뉴의 설정이 필요합니다.");
        }

        for (SetMenuComponentDto setting : settings) {
            if (!componentMenuIds.contains(setting.getComponentMenuId())) {
                throw new IllegalArgumentException("선택하지 않은 메뉴의 세트 설정이 포함되어 있습니다.");
            }
            String group = setting.getSelectGroup();
            if (group == null || group.isBlank()) {
                setting.setSelectGroup(null);
                setting.setGroupMaxSelect(null);
            } else if (setting.getGroupMaxSelect() == null || setting.getGroupMaxSelect() != 1) {
                throw new IllegalArgumentException("현재 선택 그룹은 최대 1개 선택만 지원합니다.");
            }
            if (setting.getExtraPrice() == null) {
                setting.setExtraPrice(0);
            }
        }

        Map<String, List<SetMenuComponentDto>> groups = settings.stream()
                .filter(setting -> setting.getSelectGroup() != null)
                .collect(Collectors.groupingBy(SetMenuComponentDto::getSelectGroup));
        for (Map.Entry<String, List<SetMenuComponentDto>> entry : groups.entrySet()) {
            long maxSelectCount = entry.getValue().stream()
                    .map(SetMenuComponentDto::getGroupMaxSelect)
                    .distinct()
                    .count();
            int maxSelect = entry.getValue().get(0).getGroupMaxSelect();
            if (maxSelectCount != 1 || maxSelect > entry.getValue().size()) {
                throw new IllegalArgumentException(
                        entry.getKey() + " 그룹의 최대 선택 수 설정을 확인해주세요."
                );
            }
        }
        return settings;
    }

    private void replaceSetComponents(
            Long setMenuId,
            List<SetMenuComponentDto> componentSettings
    ) {
        menuMapper.deleteSetComponents(setMenuId);

        for (SetMenuComponentDto setting : componentSettings) {
            menuMapper.insertSetComponent(
                    setMenuId,
                    setting.getComponentMenuId(),
                    setting.getSelectGroup(),
                    setting.getGroupMaxSelect(),
                    setting.getExtraPrice()
            );
        }
    }

    private void validateCategory(String category) {
        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException("올바른 메뉴 카테고리를 선택해주세요.");
        }
    }

    private String validateMenuType(String menuType, String category) {
        String normalizedType = menuType == null || menuType.isBlank()
                ? "NORMAL"
                : menuType.trim().toUpperCase();

        if (!ALLOWED_MENU_TYPES.contains(normalizedType)) {
            throw new IllegalArgumentException("올바른 메뉴 용도를 선택해주세요.");
        }
        if ("세트".equals(category) && "COMPONENT".equals(normalizedType)) {
            throw new IllegalArgumentException("세트 메뉴는 구성 전용 메뉴로 등록할 수 없습니다.");
        }

        return normalizedType;
    }

    // 메뉴 등록
    @Transactional
    public Long insert(AdminMenuRequestDto dto, MultipartFile file) {
        validateCategory(dto.getCategory());
        dto.setMenuType(validateMenuType(dto.getMenuType(), dto.getCategory()));
        List<Long> componentMenuIds = "세트".equals(dto.getCategory())
                ? validateComponentMenuIds(null, dto.getComponentMenuIds())
                : List.of();
        List<SetMenuComponentDto> componentSettings =
                validateComponentSettings(componentMenuIds, dto.getComponentSettings());

        if (file != null && !file.isEmpty()) {
            String imageUrl = saveImage(file);
            dto.setImageUrl(imageUrl);
        }

        Menu menu = new Menu();

        menu.setMenuName(dto.getMenuName());
        menu.setMenuNameEn(dto.getMenuNameEn());
        menu.setMenuType(dto.getMenuType());
        menu.setPrice("COMPONENT".equals(dto.getMenuType()) ? 0 : dto.getPrice());
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

        if (result <= 0 || menu.getMenuId() == null) {
            throw new IllegalStateException("메뉴 등록에 실패했습니다.");
        }

        if ("세트".equals(dto.getCategory())) {
            replaceSetComponents(menu.getMenuId(), componentSettings);
        }

        return menu.getMenuId();
    }

    // 메뉴 수정
    @Transactional
    public int update(
            Long menuId,
            AdminMenuRequestDto dto,
            MultipartFile file
    ) {
        validateCategory(dto.getCategory());
        dto.setMenuType(validateMenuType(dto.getMenuType(), dto.getCategory()));
        List<Long> componentMenuIds = "세트".equals(dto.getCategory())
                ? validateComponentMenuIds(menuId, dto.getComponentMenuIds())
                : List.of();
        List<SetMenuComponentDto> componentSettings =
                validateComponentSettings(componentMenuIds, dto.getComponentSettings());

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
        menu.setMenuType(dto.getMenuType());
        menu.setPrice("COMPONENT".equals(dto.getMenuType()) ? 0 : dto.getPrice());
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
            replaceSetComponents(menuId, componentSettings);

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
