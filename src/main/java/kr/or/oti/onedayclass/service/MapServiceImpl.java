package kr.or.oti.onedayclass.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.oti.onedayclass.dto.MapPlaceDTO;
import kr.or.oti.onedayclass.dto.MapRouteResponseDTO;

@Service
public class MapServiceImpl implements MapService {

    @Value("${kakao.rest-api-key}")
    private String kakaoRestApiKey;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;


    public MapServiceImpl() {

        this.restTemplate =
                new RestTemplate();

        this.objectMapper =
                new ObjectMapper();
    }


    /*
     * =========================================================
     * 1. 장소 검색
     * =========================================================
     */
    @Override
    public List<MapPlaceDTO> searchPlaces(
            String keyword) {

        try {

            URI uri =
                    UriComponentsBuilder

                        .fromHttpUrl(
                            "https://dapi.kakao.com/v2/local/search/keyword.json")

                        .queryParam(
                            "query",
                            keyword)

                        .queryParam(
                            "size",
                            10)

                        .encode()

                        .build()

                        .toUri();


            HttpEntity<Void> entity =
                    new HttpEntity<>(
                        createHeaders());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class);


            JsonNode root =
                    objectMapper.readTree(
                        response.getBody());


            JsonNode documents =
                    root.path(
                        "documents");


            List<MapPlaceDTO> placeList =
                    new ArrayList<>();


            if (!documents.isArray()
                    || documents.size() == 0) {

                return placeList;
            }


            for (JsonNode document
                    : documents) {

                MapPlaceDTO place =
                        MapPlaceDTO
                            .builder()

                            .placeId(
                                document
                                    .path("id")
                                    .asText())

                            .placeName(
                                document
                                    .path("place_name")
                                    .asText())

                            .addressName(
                                document
                                    .path("address_name")
                                    .asText())

                            .roadAddressName(
                                document
                                    .path("road_address_name")
                                    .asText())

                            .categoryName(
                                document
                                    .path("category_name")
                                    .asText())

                            .phone(
                                document
                                    .path("phone")
                                    .asText())

                            .longitude(
                                document
                                    .path("x")
                                    .asDouble())

                            .latitude(
                                document
                                    .path("y")
                                    .asDouble())

                            .placeUrl(
                                document
                                    .path("place_url")
                                    .asText())

                            .build();


                placeList.add(
                    place);
            }


            return placeList;


        } catch (Exception e) {

            throw new RuntimeException(
                "장소 검색 중 오류가 발생했습니다.",
                e);
        }
    }


    /*
     * =========================================================
     * 2. 도보 최단경로
     * =========================================================
     */
    @Override
    public MapRouteResponseDTO getShortestWalkingRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress) {


        double[] destinationCoords =
                getCoordinatesFromAddress(
                    destinationAddress);


        return getWalkingRoute(

                startName,

                startLongitude,

                startLatitude,

                destinationAddress,

                destinationCoords[0],

                destinationCoords[1]);
    }


    /*
     * =========================================================
     * 3. 대중교통 경로
     * =========================================================
     */
    @Override
    public MapRouteResponseDTO getPublicTransitRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress) {


        double[] destinationCoords =
                getCoordinatesFromAddress(
                    destinationAddress);


        try {

            URI uri =
                    UriComponentsBuilder

                        .fromHttpUrl(
                            "https://dapi.kakao.com/v2/routing/publictraffic")

                        .queryParam(
                            "start_x",
                            startLongitude)

                        .queryParam(
                            "start_y",
                            startLatitude)

                        .queryParam(
                            "s_name",
                            startName)

                        .queryParam(
                            "end_x",
                            destinationCoords[0])

                        .queryParam(
                            "end_y",
                            destinationCoords[1])

                        .queryParam(
                            "e_name",
                            destinationAddress)

                        .encode()

                        .build()

                        .toUri();


            HttpEntity<Void> entity =
                    new HttpEntity<>(
                        createHeaders());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class);


            JsonNode root =
                    objectMapper.readTree(
                        response.getBody());


            String status =
                    root
                        .path("status")
                        .asText();


            if (!"OK".equals(
                    status)) {

                throw new RuntimeException(
                    "대중교통 경로를 찾을 수 없습니다. 상태: "
                    + status);
            }


            JsonNode routes =
                    root.path(
                        "routes");


            if (!routes.isArray()
                    || routes.size() == 0) {

                throw new RuntimeException(
                    "대중교통 경로 결과가 없습니다.");
            }


            /*
             * 현재는 첫 번째 추천 경로 사용
             */
            JsonNode route =
                    routes.get(0);


            JsonNode properties =
                    route.path(
                        "properties");


            int distanceMeters =
                    properties
                        .path("totalDistance")
                        .asInt();


            int durationSeconds =
                    properties
                        .path("totalTime")
                        .asInt();


            int transfers =
                    properties
                        .path("transfers")
                        .asInt();


            int fare =
                    properties
                        .path("fare")
                        .path("value")
                        .asInt();


            String transitType =
                    properties
                        .path("type")
                        .asText();


            String landingUrl =
                    root
                        .path("properties")
                        .path("landingURL")
                        .asText();


            double distanceKm =
                    Math.round(
                        distanceMeters
                        / 1000.0
                        * 100.0)
                    / 100.0;


            int durationMinutes =
                    (int) Math.ceil(
                        durationSeconds
                        / 60.0);


            /*
             * =====================================================
             * 대중교통 경로 좌표 추출
             * =====================================================
             *
             * 각 step의:
             *
             * path.points
             *
             * 값을 모두 하나의 List에 합친다.
             */
            List<double[]> routePoints =
                    extractTransitRoutePoints(
                        route);


            return MapRouteResponseDTO
                    .builder()

                    .routeType(
                        "TRANSIT")

                    .startAddress(
                        startName)

                    .destinationAddress(
                        destinationAddress)

                    .distanceMeters(
                        distanceMeters)

                    .distanceKm(
                        distanceKm)

                    .durationSeconds(
                        durationSeconds)

                    .durationMinutes(
                        durationMinutes)

                    .transfers(
                        transfers)

                    .fare(
                        fare)

                    .transitType(
                        transitType)

                    .landingUrl(
                        landingUrl)

                    /*
                     * 지도 Polyline용 좌표
                     */
                    .routePoints(
                        routePoints)

                    .build();


        } catch (Exception e) {

            throw new RuntimeException(
                "대중교통 경로 조회 중 오류가 발생했습니다.",
                e);
        }
    }


    /*
     * =========================================================
     * 4. 자전거 최단경로
     * =========================================================
     */
    @Override
    public MapRouteResponseDTO getBicycleRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress) {


        double[] destinationCoords =
                getCoordinatesFromAddress(
                    destinationAddress);


        try {

            URI uri =
                    UriComponentsBuilder

                        .fromHttpUrl(
                            "https://dapi.kakao.com/v2/routing/bicycle")

                        .queryParam(
                            "start_x",
                            startLongitude)

                        .queryParam(
                            "start_y",
                            startLatitude)

                        .queryParam(
                            "s_name",
                            startName)

                        .queryParam(
                            "end_x",
                            destinationCoords[0])

                        .queryParam(
                            "end_y",
                            destinationCoords[1])

                        .queryParam(
                            "e_name",
                            destinationAddress)

                        .queryParam(
                            "route_mode",
                            "SHORTEST")

                        .encode()

                        .build()

                        .toUri();


            HttpEntity<Void> entity =
                    new HttpEntity<>(
                        createHeaders());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class);


            JsonNode root =
                    objectMapper.readTree(
                        response.getBody());


            String status =
                    root
                        .path("status")
                        .asText();


            if (!"OK".equals(
                    status)) {

                throw new RuntimeException(
                    "자전거 경로를 찾을 수 없습니다. 상태: "
                    + status);
            }


            JsonNode route =
                    root.path(
                        "route");


            JsonNode properties =
                    route.path(
                        "properties");


            int distanceMeters =
                    properties
                        .path("totalDistance")
                        .asInt();


            int durationSeconds =
                    properties
                        .path("totalTime")
                        .asInt();


            String landingUrl =
                    properties
                        .path("landingUrl")
                        .asText();


            double distanceKm =
                    Math.round(
                        distanceMeters
                        / 1000.0
                        * 100.0)
                    / 100.0;


            int durationMinutes =
                    (int) Math.ceil(
                        durationSeconds
                        / 60.0);


            /*
             * 자전거 실제 경로 좌표 추출
             */
            List<double[]> routePoints =
                    extractRoutePoints(
                        route);


            return MapRouteResponseDTO
                    .builder()

                    .routeType(
                        "BICYCLE")

                    .startAddress(
                        startName)

                    .destinationAddress(
                        destinationAddress)

                    .distanceMeters(
                        distanceMeters)

                    .distanceKm(
                        distanceKm)

                    .durationSeconds(
                        durationSeconds)

                    .durationMinutes(
                        durationMinutes)

                    .transfers(
                        0)

                    .fare(
                        0)

                    .transitType(
                        null)

                    .landingUrl(
                        landingUrl)

                    /*
                     * Polyline 좌표
                     */
                    .routePoints(
                        routePoints)

                    .build();


        } catch (Exception e) {

            throw new RuntimeException(
                "자전거 경로 조회 중 오류가 발생했습니다.",
                e);
        }
    }


    /*
     * =========================================================
     * 5. 클래스 주소 → 좌표 변환
     * =========================================================
     */
    private double[] getCoordinatesFromAddress(
            String address) {

        try {

            URI uri =
                    UriComponentsBuilder

                        .fromHttpUrl(
                            "https://dapi.kakao.com/v2/local/search/address.json")

                        .queryParam(
                            "query",
                            address)

                        .encode()

                        .build()

                        .toUri();


            HttpEntity<Void> entity =
                    new HttpEntity<>(
                        createHeaders());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class);


            JsonNode root =
                    objectMapper.readTree(
                        response.getBody());


            JsonNode documents =
                    root.path(
                        "documents");


            /*
             * 주소 검색 결과가 없는 경우
             *
             * 클래스 location에
             * "강남역", "잠실종합운동장"처럼
             * 장소명이 저장돼 있을 수도 있으므로
             * 장소 키워드 검색으로 다시 시도한다.
             */
            if (!documents.isArray()
                    || documents.size() == 0) {


                List<MapPlaceDTO> places =
                        searchPlaces(
                            address);


                if (places.isEmpty()) {

                    throw new RuntimeException(
                        "클래스 장소를 찾을 수 없습니다: "
                        + address);
                }


                MapPlaceDTO firstPlace =
                        places.get(0);


                return new double[] {

                    firstPlace.getLongitude(),

                    firstPlace.getLatitude()
                };
            }


            JsonNode first =
                    documents.get(0);


            /*
             * x = 경도
             */
            double longitude =
                    first
                        .path("x")
                        .asDouble();


            /*
             * y = 위도
             */
            double latitude =
                    first
                        .path("y")
                        .asDouble();


            return new double[] {

                longitude,

                latitude
            };


        } catch (Exception e) {

            throw new RuntimeException(
                "클래스 장소 좌표 변환 중 오류가 발생했습니다: "
                + address,
                e);
        }
    }


    /*
     * =========================================================
     * 6. 실제 도보 경로 API 호출
     * =========================================================
     */
    private MapRouteResponseDTO getWalkingRoute(
            String startName,
            double startLongitude,
            double startLatitude,
            String destinationAddress,
            double destinationLongitude,
            double destinationLatitude) {

        try {

            URI uri =
                    UriComponentsBuilder

                        .fromHttpUrl(
                            "https://dapi.kakao.com/v2/routing/walk")

                        .queryParam(
                            "start_x",
                            startLongitude)

                        .queryParam(
                            "start_y",
                            startLatitude)

                        .queryParam(
                            "s_name",
                            startName)

                        .queryParam(
                            "end_x",
                            destinationLongitude)

                        .queryParam(
                            "end_y",
                            destinationLatitude)

                        .queryParam(
                            "e_name",
                            destinationAddress)

                        .queryParam(
                            "route_mode",
                            "SHORTEST")

                        .encode()

                        .build()

                        .toUri();


            HttpEntity<Void> entity =
                    new HttpEntity<>(
                        createHeaders());


            ResponseEntity<String> response =
                    restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        entity,
                        String.class);


            JsonNode root =
                    objectMapper.readTree(
                        response.getBody());


            String status =
                    root
                        .path("status")
                        .asText();


            if (!"OK".equals(
                    status)) {

                throw new RuntimeException(
                    "도보 경로를 찾을 수 없습니다. 상태: "
                    + status);
            }


            JsonNode route =
                    root.path(
                        "route");


            JsonNode properties =
                    route.path(
                        "properties");


            int distanceMeters =
                    properties
                        .path("totalDistance")
                        .asInt();


            int durationSeconds =
                    properties
                        .path("totalTime")
                        .asInt();


            String landingUrl =
                    properties
                        .path("landingUrl")
                        .asText();


            double distanceKm =
                    Math.round(
                        distanceMeters
                        / 1000.0
                        * 100.0)
                    / 100.0;


            int durationMinutes =
                    (int) Math.ceil(
                        durationSeconds
                        / 60.0);


            /*
             * =====================================================
             * 도보 실제 경로 좌표 추출
             * =====================================================
             */
            List<double[]> routePoints =
                    extractRoutePoints(
                        route);


            return MapRouteResponseDTO
                    .builder()

                    .routeType(
                        "WALK")

                    .startAddress(
                        startName)

                    .destinationAddress(
                        destinationAddress)

                    .distanceMeters(
                        distanceMeters)

                    .distanceKm(
                        distanceKm)

                    .durationSeconds(
                        durationSeconds)

                    .durationMinutes(
                        durationMinutes)

                    .transfers(
                        0)

                    .fare(
                        0)

                    .transitType(
                        null)

                    .landingUrl(
                        landingUrl)

                    /*
                     * 지도 Polyline 좌표
                     */
                    .routePoints(
                        routePoints)

                    .build();


        } catch (Exception e) {

            throw new RuntimeException(
                "도보 최단경로 조회 중 오류가 발생했습니다.",
                e);
        }
    }


    /*
     * =========================================================
     * 7. 경로 좌표 추출 공통 메서드
     * =========================================================
     *
     * 카카오 경로 API의 구조:
     *
     * route
     *   └─ legs
     *       └─ steps
     *           └─ path
     *               └─ points
     *
     * points:
     *
     * [
     *   [경도, 위도],
     *   [경도, 위도],
     *   [경도, 위도]
     * ]
     *
     * 이 좌표들을 하나의 List<double[]>로 합쳐서 반환한다.
     */
    private List<double[]> extractRoutePoints(
            JsonNode routeNode) {


        /*
         * 최종 좌표 목록
         */
        List<double[]> routePoints =
                new ArrayList<>();


        /*
         * route 안의 legs 배열
         */
        JsonNode legs =
                routeNode.path(
                    "legs");


        /*
         * legs가 없으면 빈 리스트 반환
         */
        if (!legs.isArray()) {

            return routePoints;
        }


        /*
         * leg 하나씩 반복
         */
        for (JsonNode leg
                : legs) {


            /*
             * leg 안의 steps
             */
            JsonNode steps =
                    leg.path(
                        "steps");


            if (!steps.isArray()) {

                continue;
            }


            /*
             * step 하나씩 반복
             */
            for (JsonNode step
                    : steps) {


                /*
                 * step.path.points
                 */
                JsonNode points =
                        step
                            .path("path")
                            .path("points");


                /*
                 * 좌표 배열이 없으면 다음 step으로
                 */
                if (!points.isArray()) {

                    continue;
                }


                /*
                 * 각 좌표:
                 *
                 * [0] = 경도
                 * [1] = 위도
                 */
                for (JsonNode point
                        : points) {


                    /*
                     * 좌표가 정상적인 2개 값인지 확인
                     */
                    if (!point.isArray()
                            || point.size() < 2) {

                        continue;
                    }


                    double longitude =
                            point
                                .get(0)
                                .asDouble();


                    double latitude =
                            point
                                .get(1)
                                .asDouble();


                    /*
                     * DTO로 보낼 형태:
                     *
                     * double[] {
                     *     longitude,
                     *     latitude
                     * }
                     */
                    routePoints.add(
                        new double[] {

                            longitude,

                            latitude
                        }
                    );
                }
            }
        }


        return routePoints;
    }
    
    
    /*
     * =========================================================
     * 대중교통 경로 좌표 추출
     * =========================================================
     *
     * 대중교통 응답 구조:
     *
     * route
     *   └─ steps
     *       └─ path
     *           └─ points
     */
    private List<double[]> extractTransitRoutePoints(
            JsonNode routeNode) {


        List<double[]> routePoints =
                new ArrayList<>();


        /*
         * 대중교통은 route 바로 아래에 steps가 있다.
         */
        JsonNode steps =
                routeNode.path(
                    "steps");


        if (!steps.isArray()) {

            return routePoints;
        }


        /*
         * 각 이동 단계 반복
         */
        for (JsonNode step
                : steps) {


            /*
             * step.path.points
             */
            JsonNode points =
                    step
                        .path("path")
                        .path("points");


            if (!points.isArray()) {

                continue;
            }


            /*
             * 좌표 하나씩 저장
             */
            for (JsonNode point
                    : points) {


                if (!point.isArray()
                        || point.size() < 2) {

                    continue;
                }


                /*
                 * Kakao API:
                 *
                 * [0] = 경도
                 * [1] = 위도
                 */
                double longitude =
                        point
                            .get(0)
                            .asDouble();


                double latitude =
                        point
                            .get(1)
                            .asDouble();


                routePoints.add(
                    new double[] {

                        longitude,

                        latitude
                    }
                );
            }
        }


        return routePoints;
    }


    /*
     * =========================================================
     * 8. 카카오 REST API 인증 Header
     * =========================================================
     */
    private HttpHeaders createHeaders() {


        HttpHeaders headers =
                new HttpHeaders();


        headers.set(
            "Authorization",
            "KakaoAK "
            + kakaoRestApiKey);


        return headers;
    }
}