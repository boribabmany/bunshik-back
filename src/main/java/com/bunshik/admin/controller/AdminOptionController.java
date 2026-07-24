package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminOptionRequestDto;
import com.bunshik.admin.service.AdminOptionService;
import com.bunshik.common.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/api/admin/options")
@RequiredArgsConstructor
public class AdminOptionController {

    private final AdminOptionService adminOptionService;

    // 옵션 전체 조회
    @GetMapping
    public List<Option> findAll() {
        return adminOptionService.findAll();
    }

    // 옵션 한 개 조회
    @GetMapping("/{optionId}")
    public Option findById(@PathVariable Long optionId) {
        return adminOptionService.findById(optionId);
    }

    // 옵션 등록
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public int insert(
            @RequestParam("option") String option,
            @RequestParam(value = "file", required = false)
            MultipartFile file
    ) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        AdminOptionRequestDto dto =
                objectMapper.readValue(
                        option,
                        AdminOptionRequestDto.class
                );

        return adminOptionService.insert(dto, file);
    }

    // 옵션 수정
    @PutMapping(
            value = "/{optionId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public int update(
            @PathVariable Long optionId,
            @RequestParam("option") String option,
            @RequestParam(value = "file", required = false)
            MultipartFile file
    ) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        AdminOptionRequestDto dto =
                objectMapper.readValue(
                        option,
                        AdminOptionRequestDto.class
                );

        return adminOptionService.update(
                optionId,
                dto,
                file
        );
    }

    // 옵션 삭제
    @DeleteMapping("/{optionId}")
    public int delete(@PathVariable Long optionId) {
        return adminOptionService.delete(optionId);
    }
}