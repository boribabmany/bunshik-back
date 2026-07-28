package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminMenuRequestDto;
import com.bunshik.admin.service.AdminMenuService;
import com.bunshik.common.ApiResponse;
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
    public ApiResponse<List<Menu>> findAll() {
        return ApiResponse.success(
                adminMenuService.findAll()
        );
    }

    // 메뉴 한 개 조회
    @GetMapping("/{menuId}")
    public ApiResponse<Menu> findById(
            @PathVariable Long menuId
    ) {
        return ApiResponse.success(
                adminMenuService.findById(menuId)
        );
    }

    // 메뉴 등록
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Integer> insert(
            @ModelAttribute AdminMenuRequestDto dto,
            @RequestParam("file") MultipartFile file
    ) {
        int result = adminMenuService.insert(dto, file);

        return ApiResponse.success(
                result,
                "메뉴가 등록되었습니다."
        );
    }

    // 메뉴 수정
    @PutMapping(
            value = "/{menuId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Integer> update(
            @PathVariable Long menuId,
            @ModelAttribute AdminMenuRequestDto dto,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) {
        int result = adminMenuService.update(
                menuId,
                dto,
                file
        );

        return ApiResponse.success(
                result,
                "메뉴가 수정되었습니다."
        );
    }

    // 메뉴 판매중단
    @PatchMapping("/{menuId}/stop")
    public ApiResponse<Integer> stopSelling(
            @PathVariable Long menuId
    ) {
        int result = adminMenuService.stopSelling(menuId);

        return ApiResponse.success(
                result,
                "메뉴 판매가 중단되었습니다."
        );
    }

    // 메뉴 판매재개
    @PatchMapping("/{menuId}/resume")
    public ApiResponse<Integer> resumeSelling(
            @PathVariable Long menuId
    ) {
        int result = adminMenuService.resumeSelling(menuId);

        return ApiResponse.success(
                result,
                "메뉴 판매가 재개되었습니다."
        );
    }
}