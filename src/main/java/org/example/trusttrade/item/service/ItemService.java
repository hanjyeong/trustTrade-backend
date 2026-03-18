package org.example.trusttrade.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.auction.repository.AuctionRepository;
import org.example.trusttrade.global.error.NotAllowUserType;
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
import org.example.trusttrade.login.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 카테고리로 물품 조회
    public List<ItemResponseDto> findByCategoryAndType(Long categoryId, String itemType) {
        ItemType requestedType = ItemType.from(itemType);
        return switch (requestedType) {
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

        // 1) 이미지 저장
        if (imageUrls != null && !imageUrls.isEmpty()) {
            List<ItemImage> images = ItemImage.fromDto(item, imageUrls);
            log.debug("이미지 변환 완료: itemId={}, imagesToSave={}", item.getId(), images.size());

            itemImageRepository.saveAll(images);
            log.debug("이미지 저장 완료: itemId={}, savedImages={}", item.getId(), images.size());
        } else {
            log.debug("저장할 이미지 없음: itemId={}", item.getId());
        }

        // 2) 카테고리 매핑
        if (categoryIds != null && !categoryIds.isEmpty()) {
            List<Category> categories = categoryService.getValidCategories(categoryIds);
            List<Long> categoryIdList = categories.stream()
                    .map(Category::getId)
                    .toList();
            log.debug("유효 카테고리 조회 완료: itemId={}, categories={}", item.getId(), categoryIdList);

            List<ItemCategory> mappings = ItemCategory.createMappings(item, categories);
            log.debug("ItemCategory 매핑 생성 완료: itemId={}, mappingsCount={}", item.getId(), mappings.size());

            itemCategoryRepository.saveAll(mappings);
            log.debug("카테고리 매핑 저장 완료: itemId={}, savedMappings={}", item.getId(), mappings.size());
        } else {
            log.debug("매핑할 카테고리 없음: itemId={}", item.getId());
        }

        log.debug("saveItemDetails 완료: itemId={}", item.getId());
    }


    // 판매자 아이디로 상품 조회
    public List<ItemResponseDto> findBySellerAccountAndType(String sellerAccount, String itemType) {
        ItemType requestedType = ItemType.from(itemType);

        User seller = userRepository.findByUserAccount(sellerAccount)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자 계정입니다."));

        if (seller.getMemberType() != User.MemberType.BUSINESS) {
            throw new NotAllowUserType("일반 회원은 물건을 등록 할 수 없습니다");
        }

        return findItemsBySeller(seller.getId(), requestedType);
    }


    // 물품 이름으로 상품 조회
    public List<ItemResponseDto> findByItemTitleAndType(String title, String itemType) {
        return findItemsByTitle(title, ItemType.from(itemType));
    }

    private List<ItemResponseDto> findItemsBySeller(UUID sellerId, ItemType itemType) {
        return switch (itemType) {
            case PRODUCT -> productRepository.findByUser_Id(sellerId).stream()
                    .map(ItemResponseDto::fromProduct)
                    .toList();
            case AUCTION -> auctionRepository.findByUser_Id(sellerId).stream()
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


