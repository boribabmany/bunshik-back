package com.bunshik.admin.controller;

import com.bunshik.admin.service.AdminOptionService;
import com.bunshik.common.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
   @PostMapping
   public int Option (@RequestBody Option option) {
        return adminOptionService.insert(option);
   }
    // 옵션 수정
    @PutMapping("/{optionId}")
    public int update(@PathVariable Long optionId,
                      @RequestBody Option option) {

        option.setOptionId(optionId);
        return adminOptionService.update(option);
    }
    // 옵션 삭제
    @DeleteMapping("/{optionId}")
    public int delete(@PathVariable Long optionId) {
        return adminOptionService.delete(optionId);
    }
}