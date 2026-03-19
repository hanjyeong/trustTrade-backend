package org.example.trusttrade.item.service;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.item.dto.StoredImage;
import org.example.trusttrade.item.dto.request.ItemImageAttachable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
@Slf4j
public class LocalImageService implements ImageService {

    @Value("${app.upload.root}")
    private String uploadRoot;
    private Path root;

    @PostConstruct
    public void init() {
        this.root = Paths.get(uploadRoot);
        try {
            Files.createDirectories(root);
            log.info("업로드 루트 디렉터리 생성: {}", root.toAbsolutePath());
        } catch (IOException e) {
            throw new IllegalStateException("업로드 루트 디렉터리 생성 실패: " + root, e);
        }
    }

    @Override
    public List<StoredImage> uploadImages(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .limit(5)
                .map(file -> {
                    try {
                        String original = Optional.ofNullable(file.getOriginalFilename()).orElse("unknown");
                        String safeName = original.replaceAll("\\s+", "_");
                        String filename = UUID.randomUUID() + "_" + safeName;

                        Path filePath = root.resolve(filename);
                        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                        String url = "/uploads/" + filename;
                        return new StoredImage(url, filePath);
                    } catch (IOException e) {
                        throw new RuntimeException("이미지 저장 실패: " + file.getOriginalFilename(), e);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StoredImage> processImagesAndSetDto(List<MultipartFile> images, ItemImageAttachable dto) {
        List<StoredImage> storedImages = uploadImages(images);
        List<String> urls = storedImages.stream()
                .map(StoredImage::getUrl)
                .toList();

        if (!urls.isEmpty()) {
            dto.setMainImage(urls.get(0));
            if (urls.size() > 1) {
                dto.setSubImages(urls.subList(1, urls.size()));
            }
        }

        return storedImages;
    }

    @Override
    public void deleteFiles(List<StoredImage> storedImages) {
        if (storedImages == null) {
            return;
        }

        for (StoredImage si : storedImages) {
            try {
                Files.deleteIfExists(si.getAbsolutePath());
                log.debug("삭제됨: {}", si.getAbsolutePath());
            } catch (IOException ex) {
                log.warn("파일 삭제 실패: {}", si.getAbsolutePath(), ex);
            }
        }
    }
}
