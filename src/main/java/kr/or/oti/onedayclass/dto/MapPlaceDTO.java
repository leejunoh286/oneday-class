package kr.or.oti.onedayclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapPlaceDTO {

    // 카카오 장소 ID
    private String placeId;

    // 장소명
    // 예: 강남역 2호선
    private String placeName;

    // 지번 주소
    private String addressName;

    // 도로명 주소
    private String roadAddressName;

    // 장소 카테고리
    private String categoryName;

    // 전화번호
    private String phone;

    // 경도
    private double longitude;

    // 위도
    private double latitude;

    // 카카오맵 장소 상세 페이지
    private String placeUrl;
}