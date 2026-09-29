package kr.or.oti.onedayclass.service;

import java.util.List;

import kr.or.oti.onedayclass.dto.MapPlaceDTO;
import kr.or.oti.onedayclass.dto.MapRouteResponseDTO;

public interface MapService {

    List<MapPlaceDTO> searchPlaces(String keyword);


    // 도보
    MapRouteResponseDTO getShortestWalkingRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress);


    // 대중교통
    MapRouteResponseDTO getPublicTransitRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress);
    
    
    // 자전거
    MapRouteResponseDTO getBicycleRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress);
}







