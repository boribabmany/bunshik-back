package com.bunshik.admin.controller;

import com.bunshik.admin.dto.AdminOptionRequestDto;
import com.bunshik.admin.service.AdminOptionService;
import com.bunshik.common.ApiResponse;
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
    public ApiResponse<List<Option>> findAll() {

        List<Option> options = adminOptionService.findAll();

        return ApiResponse.success(options);
    }

    // 옵션 한 개 조회
    @GetMapping("/{optionId}")
    public ApiResponse<Option> findById(
            @PathVariable Long optionId
    ) {

        Option option = adminOptionService.findById(optionId);

        return ApiResponse.success(option);
    }

    // 옵션 등록
    @PostMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Integer> insert(
            @RequestParam("option") String option,
            @RequestParam(
                    value = "file",
                    required = false
            )
            MultipartFile file
    ) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        AdminOptionRequestDto dto =
                objectMapper.readValue(
                        option,
                        AdminOptionRequestDto.class
                );

        int result = adminOptionService.insert(dto, file);

        return ApiResponse.success(
                result,
                "옵션이 등록되었습니다."
        );
    }

    // 옵션 수정
    @PutMapping(
            value = "/{optionId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ApiResponse<Integer> update(
            @PathVariable Long optionId,
            @RequestParam("option") String option,
            @RequestParam(
                    value = "file",
                    required = false
            )
            MultipartFile file
    ) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        AdminOptionRequestDto dto =
                objectMapper.readValue(
                        option,
                        AdminOptionRequestDto.class
                );

        int result = adminOptionService.update(
                optionId,
                dto,
                file
        );

        return ApiResponse.success(
                result,
                "옵션이 수정되었습니다."
        );
    }

    // 옵션 삭제
    @DeleteMapping("/{optionId}")
    public ApiResponse<Integer> delete(
            @PathVariable Long optionId
    ) {

        int result = adminOptionService.delete(optionId);

        return ApiResponse.success(
                result,
                "옵션이 삭제되었습니다."
        );
    }
}