package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CommentVO {

    private Long commentId;

    private Long memberId;

    private Long classId;

    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}