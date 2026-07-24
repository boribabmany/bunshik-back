package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.service.AdminMenuService;
import com.bunshik.common.entity.Menu;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;


    // 메뉴 전체 조회
    @GetMapping
    public List<Menu> findAll() {

        return adminMenuService.findAll();

    }

    // 메뉴 한 개 조회
    @GetMapping("/{menuId}")
    public Menu findById(
            @PathVariable Long menuId
    ) {

        return adminMenuService.findById(menuId);

    }

    // 메뉴 등록 (이미지 업로드)
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public int insert(
            @ModelAttribute AdminMenuRequestDto dto,
            @RequestParam("file") MultipartFile file
    ) {

        return adminMenuService.insert(dto, file);
    }

    // 메뉴 수정 (이미지 변경 가능)
    @PutMapping(
            value = "/{menuId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public int update(
            @PathVariable Long menuId,
            @ModelAttribute AdminMenuRequestDto dto,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {

        return adminMenuService.update(
                menuId,
                dto,
                file
        );
    }

    // 메뉴 삭제
    @DeleteMapping("/{menuId}")
    public int delete(
            @PathVariable Long menuId
    ) {

        return adminMenuService.delete(menuId);
    }

}