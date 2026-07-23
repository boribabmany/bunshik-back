package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminHistoryResponse;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminHistoryService {

    private final AdminHistoryMapper adminHistoryMapper;

    public List<AdminHistoryResponse> getHistoryList() {
        return adminHistoryMapper.getHistoryList();
    }

}