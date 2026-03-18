package org.example.trusttrade.item.domain;

import java.util.Arrays;

public enum ItemType {
    PRODUCT,
    AUCTION;

    public static ItemType from(String rawItemType) {
        if (rawItemType == null || rawItemType.isBlank()) {
            throw new IllegalArgumentException("itemType은 비어 있을 수 없습니다. (PRODUCT|AUCTION)");
        }

        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(rawItemType.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "허용되지 않는 itemType: " + rawItemType + " (PRODUCT|AUCTION 중 하나를 사용)"
                ));
    }
}
