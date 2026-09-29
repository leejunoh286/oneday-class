package kr.or.oti.onedayclass.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MyPageApplicationDTO {

    // 신청 정보
    private Long applicationId;
    private Long memberId;
    private Long classId;
    private String applicationStatus;
    private LocalDateTime appliedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;

    // 클래스 정보
    private String title;
    private Integer price;
    private Integer capacity;
    private LocalDateTime classDate;
    private LocalDateTime deadline;
    private String location;
    private String classStatus;
}