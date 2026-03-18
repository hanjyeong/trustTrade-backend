package org.example.trusttrade.item.service;

import org.example.trusttrade.auction.domain.Auction;
import org.example.trusttrade.item.domain.Item;
import org.example.trusttrade.item.domain.products.Product;
import org.example.trusttrade.item.dto.response.ItemResponseDto;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;

public final class ItemResponseMapper {

    private ItemResponseMapper() {
    }

    public static ItemResponseDto toResponse(Item item) {
        Object unproxied = item instanceof HibernateProxy proxy
                ? proxy.getHibernateLazyInitializer().getImplementation()
                : item;

        if (unproxied instanceof Product product) {
            return ItemResponseDto.fromProduct(product);
        }

        if (unproxied instanceof Auction auction) {
            return ItemResponseDto.fromAuction(auction);
        }

        throw new IllegalStateException("지원하지 않는 아이템 타입: " + Hibernate.getClass(item).getSimpleName());
    }
}
