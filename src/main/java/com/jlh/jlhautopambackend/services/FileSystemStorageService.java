package com.jlh.jlhautopambackend.services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileSystemStorageService implements FileStorageService {

    @Value("${app.upload-dir}")
    private Path uploadDir;  // ex. /var/www/promo

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadDir);
    }

    @Override
    public String store(MultipartFile file) throws IOException {
        String originalName = Optional.ofNullable(file.getOriginalFilename()).orElse("");
        String safeName = StringUtils.cleanPath(originalName);
        String filename = UUID.randomUUID() + (safeName.isEmpty() ? "" : "-" + safeName);

        Path targetDir = uploadDir.toAbsolutePath().normalize();
        Files.createDirectories(targetDir);

        Path target = targetDir.resolve(filename);
        file.transferTo(target);
        return filename;
    }

    @Override
    public void delete(String path) throws IOException {
        Files.deleteIfExists(uploadDir.resolve(path));
    }
}
