package org.example.trusttrade.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.item.dto.StoredImage;
import org.example.trusttrade.item.dto.request.ItemImageAttachable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3ImageService implements ImageService {

    @Override
    public List<StoredImage> uploadImages(List<MultipartFile> files) {
        // TODO: AWS SDK 기반 S3 업로드 로직 연동
        throw new UnsupportedOperationException("S3 연동 미구현");
    }

    @Override
    public List<StoredImage> processImagesAndSetDto(List<MultipartFile> images, ItemImageAttachable dto) {
        // TODO: S3 업로드 후 URL 매핑 및 DTO 반영 로직 연동
        throw new UnsupportedOperationException("S3 연동 미구현");
    }

    @Override
    public void deleteFiles(List<StoredImage> storedImages) {
        // TODO: S3 객체 삭제 로직 연동
        throw new UnsupportedOperationException("S3 연동 미구현");
    }
}
