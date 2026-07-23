package com.bunshik.admin.service;

import com.bunshik.admin.mappers.AdminOptionMapper;
import com.bunshik.common.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOptionService {

    private final AdminOptionMapper adminOptionMapper;

    // 전체 조회
    public List<Option> findAll() {
        return adminOptionMapper.findAll();
    }

    // 상세 조회
    public Option findById(Long optionId) {
        return adminOptionMapper.findById(optionId);
    }

    // 등록
    public int insert(Option option) {
        return adminOptionMapper.insert(option);
    }

    // 수정
    public int update(Option option) {
        return adminOptionMapper.update(option);
    }

    // 삭제
    public int delete(Long optionId) {
        return adminOptionMapper.delete(optionId);
    }
}