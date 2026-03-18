package org.example.trusttrade.global.service;

import org.example.trusttrade.global.error.AddressNotFoundException;
import org.example.trusttrade.item.dto.request.DocumentDto;
import org.example.trusttrade.item.dto.request.GeoPoint;
import org.example.trusttrade.item.dto.response.KakaoApiResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MapServiceTest {

    @Mock
    private KakaoAddressSearchService kakaoAddressSearchService;

    @InjectMocks
    private MapService mapService;

    @Test
    void addressToGeocodeReturnsGeoPointWhenAddressExists() {
        DocumentDto documentDto = new DocumentDto("서울시 강남구", 127.123, 37.456);
        KakaoApiResponseDto response = new KakaoApiResponseDto(null, List.of(documentDto));

        when(kakaoAddressSearchService.requestAddressSearch("서울시 강남구")).thenReturn(response);
        when(kakaoAddressSearchService.toDouble(37.456)).thenReturn(37.456);
        when(kakaoAddressSearchService.toDouble(127.123)).thenReturn(127.123);

        GeoPoint result = mapService.addressToGeocode("서울시 강남구");

        assertEquals(37.456, result.getLat());
        assertEquals(127.123, result.getLng());
    }

    @Test
    void addressToGeocodeThrowsWhenAddressResultIsEmpty() {
        when(kakaoAddressSearchService.requestAddressSearch("없는 주소"))
                .thenReturn(new KakaoApiResponseDto(null, List.of()));

        assertThrows(AddressNotFoundException.class,
                () -> mapService.addressToGeocode("없는 주소"));
    }

    @Test
    void addressToGeocodeRejectsBlankAddress() {
        assertThrows(IllegalArgumentException.class,
                () -> mapService.addressToGeocode(" "));
    }
}
