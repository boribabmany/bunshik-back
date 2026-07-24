package com.bunshik.admin.service;

import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOptionMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminOptionService {

    private final AdminOptionMapper adminOptionMapper;
    private final AdminHistoryMapper adminHistoryMapper;

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

        int result = adminOptionMapper.insert(option);
        System.out.println("Option insert result = " + result);

        if (result > 0) {
            System.out.println("옵션 등록 변경내역 저장");

            saveHistory(
                    "옵션 등록",
                    option.getOptionName() + " 옵션이 등록되었습니다."
            );
        }

        return result;
    }

    // 수정
    public int update(Option option) {

        int result = adminOptionMapper.update(option);
        System.out.println("Option update result = " + result);

        if (result > 0) {
            System.out.println("옵션 수정 변경내역 저장");

            saveHistory(
                    "옵션 수정",
                    option.getOptionName() + " 옵션이 수정되었습니다."
            );
        }

        return result;
    }

    // 삭제
    public int delete(Long optionId) {

        // 삭제 전에 옵션 정보 조회
        Option option = adminOptionMapper.findById(optionId);

        int result = adminOptionMapper.delete(optionId);

        if (result > 0) {
            saveHistory(
                    "옵션 삭제",
                    option.getOptionName() + " 옵션이 삭제되었습니다."
            );
        }

        return result;
    }

    // 변경 내역 저장
    private void saveHistory(String title, String description) {

        System.out.println("saveHistory 실행");

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);

        System.out.println("admin_history 저장 완료");
    }
}