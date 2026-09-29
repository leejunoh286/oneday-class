package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReviewVO {

    private Long reviewId;

    private Long memberId;

    private Long classId;

    private Integer rating;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
