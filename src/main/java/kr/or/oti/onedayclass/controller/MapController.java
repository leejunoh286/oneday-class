package kr.or.oti.onedayclass.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import kr.or.oti.onedayclass.dto.MapPlaceDTO;
import kr.or.oti.onedayclass.dto.MapRouteResponseDTO;
import kr.or.oti.onedayclass.service.ClassService;
import kr.or.oti.onedayclass.service.MapService;

@RestController
public class MapController {

    /* 지도 관련 기능을 처리하는 Service */
    private final MapService mapService;


    /*
     * 클래스 정보를 조회하기 위한 Service
     *
     * classId를 이용해서
     * 클래스 장소(location)를 가져올 때 사용한다.
     */
    private final ClassService classService;


    /* 생성자 주입 */
    public MapController(
            MapService mapService,
            ClassService classService) {

        this.mapService =
                mapService;

        this.classService =
                classService;
    }


    /*
     * =========================================================
     * 1. 장소 검색
     * =========================================================
     *
     * 사용자가 입력한 장소명을 이용해서
     * 카카오 장소 검색 기능을 실행한다.
     *
     * 요청 예)
     *
     * /map/search?keyword=래미안
     *
     * /map/search?keyword=강남역
     */
    @GetMapping("/map/search")
    public List<MapPlaceDTO> searchPlaces(

            @RequestParam("keyword")
            String keyword) {


        /*
         * 검색어가 null이거나
         * 공백만 입력된 경우 검사
         */
        if (keyword == null
                || keyword.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "검색할 장소를 입력해주세요.");
        }


        /*
         * 앞뒤 공백을 제거한 후
         * MapService의 장소 검색 기능 호출
         */
        return mapService.searchPlaces(
                keyword.trim());
    }


    /*
     * =========================================================
     * 2. 도보 최단경로
     * =========================================================
     *
     * 사용자가 선택한 출발지 좌표와
     * 클래스 장소 사이의 도보 최단경로를 조회한다.
     *
     * 요청 예)
     *
     * /map/shortest-route
     * ?startName=강남역
     * &startLongitude=127.0
     * &startLatitude=37.5
     * &classId=30
     */
    @GetMapping("/map/shortest-route")
    public MapRouteResponseDTO getShortestRoute(

            /*
             * 출발지 장소 이름
             */
            @RequestParam("startName")
            String startName,

            /*
             * 출발지 경도
             */
            @RequestParam("startLongitude")
            double startLongitude,

            /*
             * 출발지 위도
             */
            @RequestParam("startLatitude")
            double startLatitude,

            /*
             * 목적지 클래스 ID
             */
            @RequestParam("classId")
            Long classId) {


        /*
         * 출발지 이름 검사
         */
        if (startName == null
                || startName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "출발지 이름이 없습니다.");
        }


        /*
         * classId를 이용해서
         * DB에서 클래스 정보를 조회한다.
         */
        var classVO =
                classService.getClass(
                        classId);


        /*
         * 클래스가 존재하지 않는 경우
         */
        if (classVO == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다.");
        }


        /*
         * 클래스의 장소를 가져온다.
         *
         * 이 장소가 경로의 목적지가 된다.
         */
        String destinationAddress =
                classVO.getLocation();


        /*
         * 클래스 장소가 비어있는 경우
         */
        if (destinationAddress == null
                || destinationAddress
                    .trim()
                    .isEmpty()) {

            throw new IllegalArgumentException(
                    "클래스 장소가 등록되어 있지 않습니다.");
        }


        /*
         * MapService에
         *
         * 출발지 이름
         * 출발지 경도
         * 출발지 위도
         * 목적지 주소
         *
         * 를 전달해서 도보 최단경로를 조회한다.
         */
        return mapService
                .getShortestWalkingRoute(

                    startName.trim(),

                    startLongitude,

                    startLatitude,

                    destinationAddress.trim()
                );
    }


    /*
     * =========================================================
     * 3. 대중교통 경로
     * =========================================================
     *
     * 출발지에서 클래스 장소까지
     * 대중교통 경로를 조회한다.
     *
     * 요청 예)
     *
     * /map/public-transit
     * ?startName=강남역
     * &startLongitude=127.0
     * &startLatitude=37.5
     * &classId=30
     */
    @GetMapping("/map/public-transit")
    public MapRouteResponseDTO getPublicTransitRoute(

            /*
             * 출발지 장소 이름
             */
            @RequestParam("startName")
            String startName,

            /*
             * 출발지 경도
             */
            @RequestParam("startLongitude")
            double startLongitude,

            /*
             * 출발지 위도
             */
            @RequestParam("startLatitude")
            double startLatitude,

            /*
             * 클래스 ID
             */
            @RequestParam("classId")
            Long classId) {


        /*
         * 출발지 이름 검사
         */
        if (startName == null
                || startName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "출발지 이름이 없습니다.");
        }


        /*
         * 클래스 조회
         */
        var classVO =
                classService.getClass(
                        classId);


        /*
         * 클래스가 존재하지 않는 경우
         */
        if (classVO == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다.");
        }


        /*
         * 목적지 주소
         */
        String destinationAddress =
                classVO.getLocation();


        /*
         * 목적지 주소 검사
         */
        if (destinationAddress == null
                || destinationAddress
                    .trim()
                    .isEmpty()) {

            throw new IllegalArgumentException(
                    "클래스 장소가 등록되어 있지 않습니다.");
        }


        /*
         * 대중교통 경로 조회
         */
        return mapService
                .getPublicTransitRoute(

                    startName.trim(),

                    startLongitude,

                    startLatitude,

                    destinationAddress.trim()
                );
    }


    /*
     * =========================================================
     * 4. 자전거 최단경로
     * =========================================================
     *
     * 출발지에서 클래스 장소까지
     * 자전거 최단경로를 조회한다.
     *
     * 요청 예)
     *
     * /map/bicycle
     * ?startName=강남역
     * &startLongitude=127.0
     * &startLatitude=37.5
     * &classId=30
     */
    @GetMapping("/map/bicycle")
    public MapRouteResponseDTO getBicycleRoute(

            /*
             * 출발지 장소 이름
             */
            @RequestParam("startName")
            String startName,

            /*
             * 출발지 경도
             */
            @RequestParam("startLongitude")
            double startLongitude,

            /*
             * 출발지 위도
             */
            @RequestParam("startLatitude")
            double startLatitude,

            /*
             * 목적지 클래스 ID
             */
            @RequestParam("classId")
            Long classId) {


        /*
         * 출발지 이름 검사
         */
        if (startName == null
                || startName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "출발지 이름이 없습니다.");
        }


        /*
         * 클래스 조회
         */
        var classVO =
                classService.getClass(
                        classId);


        /*
         * 클래스가 존재하지 않는 경우
         */
        if (classVO == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다.");
        }


        /*
         * 클래스 장소 가져오기
         */
        String destinationAddress =
                classVO.getLocation();


        /*
         * 클래스 장소가 없는 경우
         */
        if (destinationAddress == null
                || destinationAddress
                    .trim()
                    .isEmpty()) {

            throw new IllegalArgumentException(
                    "클래스 장소가 등록되어 있지 않습니다.");
        }


        /*
         * 자전거 최단경로 조회
         */
        return mapService
                .getBicycleRoute(

                    startName.trim(),

                    startLongitude,

                    startLatitude,

                    destinationAddress.trim()
                );
    }
}                                                                                                                                                      