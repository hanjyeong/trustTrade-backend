package org.example.trusttrade.item.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ItemTypeTest {

    @Test
    void fromAcceptsCaseInsensitiveValue() {
        assertEquals(ItemType.PRODUCT, ItemType.from("product"));
        assertEquals(ItemType.AUCTION, ItemType.from(" AUCTION "));
    }

    @Test
    void fromRejectsInvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> ItemType.from("BOOK"));
        assertThrows(IllegalArgumentException.class, () -> ItemType.from(" "));
    }
}
