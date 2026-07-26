package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminHistoryResponseDto;
import com.bunshik.admin.service.AdminHistoryService;
import com.bunshik.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/history")
public class AdminHistoryController {

    private final AdminHistoryService adminHistoryService;

    @GetMapping
    public ApiResponse<List<AdminHistoryResponseDto>> getHistoryList() {

        return ApiResponse.success(
                adminHistoryService.getHistoryList()
        );
    }
}