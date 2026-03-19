package org.example.trusttrade.item.service;

import org.example.trusttrade.item.dto.StoredImage;
import org.example.trusttrade.item.dto.request.ItemImageAttachable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ImageService {

    List<StoredImage> uploadImages(List<MultipartFile> files);

    List<StoredImage> processImagesAndSetDto(List<MultipartFile> images, ItemImageAttachable dto);

    void deleteFiles(List<StoredImage> storedImages);
}
