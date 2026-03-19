package org.example.trusttrade.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.item.domain.products.Product;
import org.example.trusttrade.item.dto.request.BasicItemDto;
import org.example.trusttrade.item.dto.response.ItemResponseDto;
import org.example.trusttrade.item.dto.response.ProductResponseDto;
import org.example.trusttrade.item.repository.ProductRepository;
import org.example.trusttrade.login.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ItemRegistrationService itemRegistrationService;
    private final ProductRepository productRepository;


    // 일반 물품 등록
    @Transactional
    public void registerBasicItem(BasicItemDto basicItemDto, User seller, List<MultipartFile> images) {
        itemRegistrationService.registerBasicItem(basicItemDto, seller, images);
    }

    // 일반 물품 전체 조회
    public List<ItemResponseDto> getBasicItems() {
        List<Product> items = productRepository.findAll();
        return items.stream()
                .map(ItemResponseDto::fromProduct)
                .toList();
    }


    // 5km 이내의 물품 조회
    public List<ProductResponseDto> findProductsNearby(double lat, double lng) {
        List<Product> products = productRepository.findNearby(lat, lng);
        return products.stream()
                .map(product -> ProductResponseDto.builder()
                        .item_id(product.getId())
                        .sellerAccount(product.getSeller().getUserAccount()) // 판매자명 대신 계정 사용
                        .title(product.getName())
                        .price(product.getProductPrice())
                        .latitude(product.getProductLocation().getLatitude())
                        .longitude(product.getProductLocation().getLongitude())
                        .build())
                .toList();
    }

}
