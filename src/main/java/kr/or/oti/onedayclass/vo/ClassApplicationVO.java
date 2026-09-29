package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ClassApplicationVO {
	
	// 신청 번호(PK)
	private Long applicationId;
	
	// 신청한 회원 번호(FK)
	private Long memberId;
	
	// 신청한 클래스 번호(FK)
	private Long classId;
	
	/*
	 * PENDING : 승인 대기
	 * APPROVED : 승인
	 * REJECTED : 거절
	 * CANCELLED : 신청 취소
	 * COMPLETED : 수강 완료
	*/
	private String status;
	
	// 신청 일시
	private LocalDateTime appliedAt;
	
	// 승인 일시
	private LocalDateTime approvedAt;
	
	// 거절 일시
	private LocalDateTime rejectedAt;
	
	// 수강 완료 일시
	private LocalDateTime completedAt;
	
	// 신청 취소 일시
	private LocalDateTime cancelledAt;
}
