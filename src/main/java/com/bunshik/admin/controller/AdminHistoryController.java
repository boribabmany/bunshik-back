package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminHistoryResponse;
import com.bunshik.admin.service.AdminHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/history")
public class AdminHistoryController {

    private final AdminHistoryService adminHistoryService;

    @GetMapping
    public List<AdminHistoryResponse> getHistoryList() {
        return adminHistoryService.getHistoryList();
    }
}