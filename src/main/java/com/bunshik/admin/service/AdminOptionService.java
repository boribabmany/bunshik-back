package com.bunshik.admin.service;

import com.bunshik.admin.dto.AdminOptionRequestDto;
import com.bunshik.admin.mappers.AdminHistoryMapper;
import com.bunshik.admin.mappers.AdminOptionMapper;
import com.bunshik.common.entity.AdminHistory;
import com.bunshik.common.entity.Option;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOptionService {

    private final AdminOptionMapper adminOptionMapper;
    private final AdminHistoryMapper adminHistoryMapper;

    private final String uploadPath = "uploads/options/";

    // 전체 조회
    public List<Option> findAll() {
        return adminOptionMapper.findAll();
    }

    // 상세 조회
    public Option findById(Long optionId) {
        return adminOptionMapper.findById(optionId);
    }

    // 등록
    public int insert(
            AdminOptionRequestDto dto,
            MultipartFile file) {

        String imageUrl = saveImage(file);

        if (imageUrl != null) {
            dto.setOptionImage(imageUrl);
        }

        Option option = new Option();

        option.setOptionName(dto.getOptionName());
        option.setOptionNameEn(dto.getOptionNameEn());
        option.setOptionPrice(dto.getOptionPrice());
        option.setOptionImage(dto.getOptionImage());
        option.setOptionIsAvailable(dto.getOptionIsAvailable());

        // 새 옵션은 기본적으로 표시
        option.setIsVisible(true);

        int result = adminOptionMapper.insert(option);

        if (result > 0) {
            saveHistory(
                    "옵션 등록",
                    dto.getOptionName() + " 옵션이 등록되었습니다.");
        }

        return result;
    }

    // 수정
    public int update(
            Long optionId,
            AdminOptionRequestDto dto,
            MultipartFile file) {

        Option oldOption = adminOptionMapper.findById(optionId);

        String imageUrl = saveImage(file);

        if (imageUrl == null && oldOption != null) {
            imageUrl = oldOption.getOptionImage();
        }

        Option option = new Option();

        option.setOptionId(optionId);
        option.setOptionName(dto.getOptionName());
        option.setOptionNameEn(dto.getOptionNameEn());
        option.setOptionPrice(dto.getOptionPrice());
        option.setOptionImage(imageUrl);
        option.setOptionIsAvailable(dto.getOptionIsAvailable());

        int result = adminOptionMapper.update(option);

        if (result > 0) {
            if (file != null && !file.isEmpty()) {
                deleteImage(oldOption);
            }

            saveHistory(
                    "옵션 수정",
                    dto.getOptionName() + " 옵션이 수정되었습니다.");
        }

        return result;
    }

    // 판매중단
    public int stopSelling(Long optionId) {

        Option option = adminOptionMapper.findById(optionId);

        int result = adminOptionMapper.stopSelling(optionId);

        if (result > 0) {
            String optionName = option != null
                    ? option.getOptionName()
                    : "옵션(ID: " + optionId + ")";

            saveHistory(
                    "옵션 판매중단",
                    optionName + " 옵션의 판매가 중단되었습니다.");
        }

        return result;
    }

    // 판매재개
    public int resumeSelling(Long optionId) {

        Option option = adminOptionMapper.findById(optionId);

        int result = adminOptionMapper.resumeSelling(optionId);

        if (result > 0) {
            String optionName = option != null
                    ? option.getOptionName()
                    : "옵션(ID: " + optionId + ")";

            saveHistory(
                    "옵션 판매재개",
                    optionName + " 옵션의 판매가 재개되었습니다.");
        }

        return result;
    }

    // 이미지 저장
    private String saveImage(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            String originalName = file.getOriginalFilename();

            if (originalName == null || originalName.isBlank()) {
                originalName = "image";
            }

            String saveName = UUID.randomUUID() + "_" + originalName;

            Path uploadDir = Paths.get(uploadPath);
            Files.createDirectories(uploadDir);

            Path savePath = uploadDir.resolve(saveName);

            Files.copy(
                    file.getInputStream(),
                    savePath,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/images/options/" + saveName;

        } catch (IOException e) {
            throw new RuntimeException(
                    "옵션 이미지 저장 실패",
                    e);
        }
    }

    // 이미지 삭제
    private void deleteImage(Option option) {

        if (option == null
                || option.getOptionImage() == null
                || option.getOptionImage().isBlank()) {
            return;
        }

        try {
            String fileName = Paths.get(option.getOptionImage())
                    .getFileName()
                    .toString();

            Path imagePath = Paths.get(uploadPath)
                    .resolve(fileName);

            Files.deleteIfExists(imagePath);

        } catch (IOException e) {
            throw new RuntimeException(
                    "옵션 이미지 삭제 실패",
                    e);
        }
    }

    // 관리자 변경 내역 저장
    private void saveHistory(
            String title,
            String description) {

        AdminHistory history = new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}