package kr.or.oti.onedayclass.dto;

import lombok.Data;

@Data
public class PageRequestDTO {

    /* 현재 페이지 번호
       기본값 : 1페이지 */
    private int page = 1;


    /* 한 페이지에 보여줄 데이터 개수
       기본값 : 10개 */
    private int size = 10;


    /* 잘못된 페이지 값 보정 */
    public void normalize() {

        if (page < 1) {
            page = 1;
        }

        if (size <= 0) {
            size = 10;
        }
    }

    public int getOffset() {

        normalize();

        return (page - 1) * size;
    }
}