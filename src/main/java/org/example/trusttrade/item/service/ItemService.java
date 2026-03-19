package org.example.trusttrade.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.auction.repository.AuctionRepository;
import org.example.trusttrade.item.domain.Category;
import org.example.trusttrade.item.domain.Item;
import org.example.trusttrade.item.domain.ItemCategory;
import org.example.trusttrade.item.domain.ItemImage;
import org.example.trusttrade.item.domain.ItemType;
import org.example.trusttrade.item.dto.response.ItemResponseDto;
import org.example.trusttrade.item.repository.ItemCategoryRepository;
import org.example.trusttrade.item.repository.ItemImageRepository;
import org.example.trusttrade.item.repository.ProductRepository;
import org.example.trusttrade.login.domain.User;
import org.example.trusttrade.login.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private final ItemImageRepository itemImageRepository;
    private final ItemCategoryRepository itemCategoryRepository;
    private final CategoryService categoryService;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;
    private final UserService userService;

    // 카테고리로 물품 조회
    public List<ItemResponseDto> findByCategoryAndType(Long categoryId, ItemType itemType) {
        return switch (itemType) {
            case PRODUCT -> mapToResponses(productRepository.findByCategoryId(categoryId));
            case AUCTION -> mapToResponses(auctionRepository.findByCategoryId(categoryId));
        };
    }


    @Transactional
    public void saveItemDetails(Item item, List<String> imageUrls, List<Integer> categoryIds) {
        log.debug("saveItemDetails 시작: itemId={}, imageCount={}, categoryCount={}",
                item.getId(),
                imageUrls == null ? 0 : imageUrls.size(),
                categoryIds == null ? 0 : categoryIds.size());

        saveImages(item, imageUrls);
        saveCategories(item, categoryIds);

        log.debug("saveItemDetails 완료: itemId={}", item.getId());
    }


    // 판매자 아이디로 상품 조회
    public List<ItemResponseDto> findBySellerAccountAndType(String sellerAccount, ItemType itemType) {
        User seller = userService.validateBusinessUserByAccount(sellerAccount);
        return findItemsBySeller(seller.getId(), itemType);
    }


    // 물품 이름으로 상품 조회
    public List<ItemResponseDto> findByItemTitleAndType(String title, ItemType itemType) {
        return findItemsByTitle(title, itemType);
    }

    private void saveImages(Item item, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            log.debug("저장할 이미지 없음: itemId={}", item.getId());
            return;
        }

        List<ItemImage> images = ItemImage.fromDto(item, imageUrls);
        log.debug("이미지 변환 완료: itemId={}, imagesToSave={}", item.getId(), images.size());

        itemImageRepository.saveAll(images);
        log.debug("이미지 저장 완료: itemId={}, savedImages={}", item.getId(), images.size());
    }

    private void saveCategories(Item item, List<Integer> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            log.debug("매핑할 카테고리 없음: itemId={}", item.getId());
            return;
        }

        List<Category> categories = categoryService.getValidCategories(categoryIds);
        List<Long> categoryIdList = categories.stream()
                .map(Category::getId)
                .toList();
        log.debug("유효 카테고리 조회 완료: itemId={}, categories={}", item.getId(), categoryIdList);

        List<ItemCategory> mappings = ItemCategory.createMappings(item, categories);
        log.debug("ItemCategory 매핑 생성 완료: itemId={}, mappingsCount={}", item.getId(), mappings.size());

        itemCategoryRepository.saveAll(mappings);
        log.debug("카테고리 매핑 저장 완료: itemId={}, savedMappings={}", item.getId(), mappings.size());
    }

    private List<ItemResponseDto> findItemsBySeller(UUID sellerId, ItemType itemType) {
        return switch (itemType) {
            case PRODUCT -> productRepository.findBySeller_Id(sellerId).stream()
                    .map(ItemResponseDto::fromProduct)
                    .toList();
            case AUCTION -> auctionRepository.findBySeller_Id(sellerId).stream()
                    .map(ItemResponseDto::fromAuction)
                    .toList();
        };
    }

    private List<ItemResponseDto> findItemsByTitle(String title, ItemType itemType) {
        return switch (itemType) {
            case PRODUCT -> productRepository.findByTitleContainingWithSeller(title).stream()
                    .map(ItemResponseDto::fromProduct)
                    .toList();
            case AUCTION -> auctionRepository.findByTitleContainingWithSeller(title).stream()
                    .map(ItemResponseDto::fromAuction)
                    .toList();
        };
    }

    private List<ItemResponseDto> mapToResponses(List<? extends Item> items) {
        return items.stream()
                .map(ItemResponseMapper::toResponse)
                .toList();
    }
}


