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

    private final Path uploadPath =
            Paths.get(System.getProperty("user.dir"), "uploads")
                    .toAbsolutePath()
                    .normalize();

    @GetMapping("/uploads/{folder}/{filename}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String folder,
            @PathVariable String filename
    ) {
        try {
            Path filePath = uploadPath
                    .resolve(folder)
                    .resolve(filename)
                    .normalize();

            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
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