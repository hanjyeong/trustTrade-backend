package org.example.trusttrade.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.auction.domain.Auction;
import org.example.trusttrade.auction.dto.AuctionItemDto;
import org.example.trusttrade.auction.repository.AuctionRepository;
import org.example.trusttrade.item.domain.Item;
import org.example.trusttrade.item.domain.products.Product;
import org.example.trusttrade.item.dto.StoredImage;
import org.example.trusttrade.item.dto.request.AbstractItemImageDto;
import org.example.trusttrade.item.dto.request.BasicItemDto;
import org.example.trusttrade.item.repository.ProductRepository;
import org.example.trusttrade.login.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRegistrationService {

    private final ImageService imageService;
    private final ItemService itemService;
    private final ProductRepository productRepository;
    private final AuctionRepository auctionRepository;

    @Transactional
    public void registerBasicItem(BasicItemDto basicItemDto, User seller, List<MultipartFile> images) {
        registerItem(
                basicItemDto,
                basicItemDto.getCategoryIds(),
                images,
                () -> Product.fromDto(basicItemDto, seller),
                productRepository::save
        );
    }

    @Transactional
    public void registerAuctionItem(AuctionItemDto auctionItemDto, User seller, List<MultipartFile> images) {
        registerItem(
                auctionItemDto,
                auctionItemDto.getCategoryIds(),
                images,
                () -> Auction.fromDto(auctionItemDto, seller),
                auctionRepository::save
        );
    }

    private <T extends AbstractItemImageDto, I extends Item> void registerItem(
            T itemDto,
            List<Integer> categoryIds,
            List<MultipartFile> images,
            Supplier<I> itemFactory,
            Function<I, I> saveAction
    ) {
        List<StoredImage> storedImages = List.of();

        try {
            storedImages = imageService.processImagesAndSetDto(images, itemDto);
            I savedItem = saveAction.apply(itemFactory.get());
            itemService.saveItemDetails(savedItem, itemDto.getImageUrls(), categoryIds);
        } catch (Exception e) {
            imageService.deleteFiles(storedImages);
            log.error("물품 등록 중 오류 발생", e);
            throw new RuntimeException("물품 등록 중 오류 발생", e);
        }
    }
}
