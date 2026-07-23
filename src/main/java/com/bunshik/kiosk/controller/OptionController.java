package com.bunshik.kiosk.controller;

import com.bunshik.common.ApiResponse;
import com.bunshik.kiosk.dto.OptionResponseDto;
import com.bunshik.kiosk.mapper.OptionMapper;
import com.bunshik.kiosk.service.OptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/options")
@RequiredArgsConstructor
public class OptionController {

    private final OptionService optionService;

    @GetMapping
    public ApiResponse<List<OptionResponseDto>> getOptions() {
        return ApiResponse.success(optionService.getOptions());
    }

}