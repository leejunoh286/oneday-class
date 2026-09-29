package kr.or.oti.onedayclass.dto;

import lombok.Data;

@Data
public class ClassSearchDTO {

    // 검색 종류
    // TITLE / CONTENT / TITLE_CONTENT
    private String searchType;

    // 검색어
    private String keyword;

    // 카테고리 검색
    private Long categoryId;

    // 현재 페이지
    private int page = 1;

    // 한 페이지에 표시할 클래스 개수
    private int size = 10;

    // 전체 데이터 개수
    private int totalCount;

    // 전체 페이지 개수
    private int totalPages;


    // MySQL LIMIT에서 사용할 시작 위치
    public int getOffset() {

        if (page < 1) {
            page = 1;
        }

        return (page - 1) * size;
    }


    // 전체 데이터 개수를 기준으로
    // 전체 페이지 수 계산
    public void calculateTotalPages() {

        if (size <= 0) {
            size = 10;
        }

        totalPages =
                (int) Math.ceil(
                        (double) totalCount / size);
    }
}