package kr.or.oti.onedayclass.dto;

import lombok.Data;

@Data
public class PageResponseDTO {

    // 현재 페이지
    private int page;

    // 한 페이지에 보여줄 데이터 개수
    private int size;

    // 검색 조건에 맞는 전체 데이터 개수
    private int totalCount;

    // 전체 페이지 개수
    private int totalPages;


    public PageResponseDTO(
            int page,
            int size,
            int totalCount) {

        this.page = page;
        this.size = size;
        this.totalCount = totalCount;

        /*
         * 전체 페이지 수 계산
         *
         * 예)
         * 20개 -> 2페이지
         * 21개 -> 3페이지
         */
        this.totalPages =
                (totalCount + size - 1)
                / size;
    }
}