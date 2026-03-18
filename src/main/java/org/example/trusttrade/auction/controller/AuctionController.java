package org.example.trusttrade.auction.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.trusttrade.auction.domain.Auction;
import org.example.trusttrade.auction.dto.AuctionDetailsResponse;
import org.example.trusttrade.auction.dto.AuctionItemDto;
import org.example.trusttrade.auction.dto.AuctionResDto;
import org.example.trusttrade.auction.service.AuctionService;
import org.example.trusttrade.global.dto.MessageResponse;
import org.example.trusttrade.login.domain.User;
import org.example.trusttrade.login.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
@Slf4j
public class AuctionController {

    private final AuctionService auctionService;
    private final UserService userService;

    //경매 조회 by 판매자
    @GetMapping("/{sellerId}/list")
    public ResponseEntity<List<AuctionResDto>> auctionsBySellerId(@PathVariable("sellerId") UUID sellerId) {
        List<Auction> auctions = auctionService.getAuctionsBySeller(sellerId);

        List<AuctionResDto> response = auctions.stream()
                .map(AuctionResDto::fromEntity)
                .toList();

        return ResponseEntity.ok(response);

    }

    // 경매 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<AuctionItemDto>> auctions() {
        List<Auction> auctions = auctionService.getAuctions();

        List<AuctionItemDto> response = auctions.stream()
                .map(AuctionItemDto::fromEntity)
                .toList();

        return ResponseEntity.ok(response);
    }

    //경매 페이지 + 낙찰 페이지
    @GetMapping("/{auctionId}/details")
    public ResponseEntity<AuctionDetailsResponse> auctionDetails(@PathVariable("auctionId") Long auctionId) {
        Auction auction = auctionService.getAuctionById(auctionId);
        return ResponseEntity.ok(AuctionDetailsResponse.from(auction));
    }

    // 경매 물품 등록
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> registerAuction(
            @RequestPart("auction_item") @Valid AuctionItemDto auctionItemDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        User seller = userService.validateBusinessUser(auctionItemDto.getSellerId());
        auctionService.registerAuctionItem(auctionItemDto, seller, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("경매 물품이 성공적으로 등록되었습니다."));
    }
}
