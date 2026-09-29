package kr.or.oti.onedayclass.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MemberDTO {
	
	private Long memberId;
	private String loginId;
	private String password;
	private String name;
	private String nickname;
	private String email;
	private String phone;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
