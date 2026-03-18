package org.example.trusttrade.global.service;

import lombok.RequiredArgsConstructor;
import org.example.trusttrade.global.error.AddressNotFoundException;
import org.example.trusttrade.item.dto.request.DocumentDto;
import org.example.trusttrade.item.dto.request.GeoPoint;
import org.example.trusttrade.item.dto.response.KakaoApiResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MapService {

    private final KakaoAddressSearchService kakaoAddressSearchService;

    // 주소를 위도,경도 값으로 변환하는 메서드
    public GeoPoint addressToGeocode(String address){
        if (!StringUtils.hasText(address)) {
            throw new IllegalArgumentException("주소는 비어 있을 수 없습니다.");
        }

        KakaoApiResponseDto kakao = kakaoAddressSearchService.requestAddressSearch(address);
        if (kakao == null || kakao.getDocumentList() == null || kakao.getDocumentList().isEmpty()) {
            throw new AddressNotFoundException("주소를 찾을 수 없습니다: " + address);
        }

        DocumentDto doc = kakao.getDocumentList().getFirst();

        double lat = kakaoAddressSearchService.toDouble(doc.getLatitude());
        double lng = kakaoAddressSearchService.toDouble(doc.getLongitude());

        return new GeoPoint(lat, lng);
    }


}
