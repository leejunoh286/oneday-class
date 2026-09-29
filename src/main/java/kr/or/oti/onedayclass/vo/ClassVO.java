package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClassVO {

    private Long classId;

    // 클래스를 개설한 회원
    private Long hostId;
    
    // 개설자 로그인 아이디
    private String hostLoginId;
    
    // 개설자 닉네임
    private String hostNickname;

    // 카테고리
    private Integer categoryId;

    private String title;

    private String description;

    private Integer price;

    private Integer capacity;

    private LocalDateTime classDate;

    private LocalDateTime deadline;

    private String location;

    // RECRUITING / CLOSED / COMPLETED
    private String status;

    private Integer viewCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
