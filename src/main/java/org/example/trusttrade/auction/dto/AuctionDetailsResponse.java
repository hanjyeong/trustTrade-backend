package org.example.trusttrade.auction.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.trusttrade.auction.domain.Auction;
import org.example.trusttrade.auction.domain.AuctionStatus;

@Getter
@Builder
public class AuctionDetailsResponse {
    private String message;
    private AuctionResDto auction;

    public static AuctionDetailsResponse from(Auction auction) {
        String message = auction.getAuctionStatus() == AuctionStatus.CLOSED
                ? "경매가 종료된 상태입니다."
                : "경매가 진행 중입니다.";

        return AuctionDetailsResponse.builder()
                .message(message)
                .auction(AuctionResDto.fromEntity(auction))
                .build();
    }
}
