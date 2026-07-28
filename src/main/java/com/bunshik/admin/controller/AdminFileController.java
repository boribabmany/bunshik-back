package com.bunshik.admin.controller;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class AdminFileController {

    // 프로젝트 실행 위치 기준 절대경로로 고정
    private final String uploadPath = System.getProperty("user.dir") + "/uploads/";

    @GetMapping("/uploads/{folder}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String folder,
            @PathVariable String filename
    ) {
        try {
            Path filePath = Paths.get(uploadPath)
                    .resolve(folder)
                    .resolve(filename)
                    .normalize();

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}