package kr.or.oti.onedayclass.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapRouteResponseDTO {

    // WALK / TRANSIT / BICYCLE
    private String routeType;

    // 출발지
    private String startAddress;

    // 도착지
    private String destinationAddress;

    // 거리(m)
    private int distanceMeters;

    // 거리(km)
    private double distanceKm;

    // 소요시간(초)
    private int durationSeconds;

    // 소요시간(분)
    private int durationMinutes;

    // 환승 횟수
    // 도보/자전거에서는 0
    private int transfers;

    // 예상 요금
    // 대중교통에서만 사용
    private int fare;

    // 대중교통 종류
    // BUS / SUBWAY / BUS_AND_SUBWAY
    private String transitType;

    // 카카오맵 상세보기 URL
    private String landingUrl;
    
    // 지도에 Polyline을 그리기 위한 경로 좌표
    private List<double[]> routePoints;
}








