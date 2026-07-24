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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminOptionService {

    private final AdminOptionMapper adminOptionMapper;
    private final AdminHistoryMapper adminHistoryMapper;

    // 옵션 이미지 저장 경로
    private final String uploadPath = "uploads/options/";

    // 옵션 전체 조회
    public List<Option> findAll() {
        return adminOptionMapper.findAll();
    }

    // 옵션 한 개 조회
    public Option findById(Long optionId) {
        return adminOptionMapper.findById(optionId);
    }

    // 옵션 등록
    public int insert(
            AdminOptionRequestDto dto,
            MultipartFile file
    ) {

        // 이미지 파일이 존재하면 저장
        if (file != null && !file.isEmpty()) {

            try {

                String originalName = file.getOriginalFilename();

                if (originalName == null) {
                    originalName = "image";
                }

                String saveName =
                        UUID.randomUUID() + "_" + originalName;

                Path uploadDir = Paths.get(uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path savePath = uploadDir.resolve(saveName);

                Files.copy(
                        file.getInputStream(),
                        savePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // DB에는 이미지 조회 URL 저장
                dto.setOptionImage(
                        "/images/options/" + saveName
                );

            } catch (IOException e) {
                throw new RuntimeException(
                        "옵션 이미지 저장 실패",
                        e
                );
            }
        }

        Option option = new Option();

        option.setOptionName(dto.getOptionName());
        option.setOptionPrice(dto.getOptionPrice());
        option.setOptionImage(dto.getOptionImage());
        option.setOptionIsAvailable(
                dto.getOptionIsAvailable()
        );

        int result = adminOptionMapper.insert(option);

        if (result > 0) {

            saveHistory(
                    "옵션 등록",
                    dto.getOptionName()
                            + " 옵션이 등록되었습니다."
            );
        }

        return result;
    }

    // 옵션 수정
    public int update(
            Long optionId,
            AdminOptionRequestDto dto,
            MultipartFile file
    ) {

        // 새로운 이미지가 있으면 저장
        if (file != null && !file.isEmpty()) {

            try {

                String originalName =
                        file.getOriginalFilename();

                if (originalName == null) {
                    originalName = "image";
                }

                String saveName =
                        UUID.randomUUID() + "_" + originalName;

                Path uploadDir = Paths.get(uploadPath);

                if (!Files.exists(uploadDir)) {
                    Files.createDirectories(uploadDir);
                }

                Path savePath = uploadDir.resolve(saveName);

                Files.copy(
                        file.getInputStream(),
                        savePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                dto.setOptionImage(
                        "/images/options/" + saveName
                );

            } catch (IOException e) {
                throw new RuntimeException(
                        "옵션 이미지 수정 저장 실패",
                        e
                );
            }
        }

        Option option = new Option();

        option.setOptionId(optionId);
        option.setOptionName(dto.getOptionName());
        option.setOptionPrice(dto.getOptionPrice());

        // 새로운 이미지가 없으면 기존 이미지 유지
        if (dto.getOptionImage() != null
                && !dto.getOptionImage().isBlank()) {

            option.setOptionImage(
                    dto.getOptionImage()
            );

        } else {

            Option oldOption =
                    adminOptionMapper.findById(optionId);

            if (oldOption != null) {
                option.setOptionImage(
                        oldOption.getOptionImage()
                );
            }
        }

        option.setOptionIsAvailable(
                dto.getOptionIsAvailable()
        );

        int result =
                adminOptionMapper.update(option);

        if (result > 0) {

            saveHistory(
                    "옵션 수정",
                    dto.getOptionName()
                            + " 옵션이 수정되었습니다."
            );
        }

        return result;
    }

    // 옵션 삭제
    public int delete(Long optionId) {

        // 삭제 전에 옵션 정보 조회
        Option option =
                adminOptionMapper.findById(optionId);

        int result =
                adminOptionMapper.delete(optionId);

        if (result > 0) {

            // DB 삭제 성공 후 이미지 파일 삭제
            if (option != null
                    && option.getOptionImage() != null
                    && !option.getOptionImage().isBlank()) {

                try {

                    String fileName =
                            Paths.get(option.getOptionImage())
                                    .getFileName()
                                    .toString();

                    Path imagePath =
                            Paths.get(uploadPath)
                                    .resolve(fileName);

                    Files.deleteIfExists(imagePath);

                } catch (IOException e) {
                    throw new RuntimeException(
                            "옵션 이미지 삭제 실패",
                            e
                    );
                }
            }

            saveHistory(
                    "옵션 삭제",
                    "옵션(ID: "
                            + optionId
                            + ")이 삭제되었습니다."
            );
        }

        return result;
    }

    // 관리자 변경 내역 저장
    private void saveHistory(
            String title,
            String description
    ) {

        AdminHistory history =
                new AdminHistory();

        history.setAdminId(1);
        history.setTitle(title);
        history.setDescription(description);

        adminHistoryMapper.insertHistory(history);
    }
}