package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MemberVO {
	
	private Long memberId;
	private Integer roleId;
	private String loginId;
	private String password;
	private String name;
	private String nickname;
	private String email;
	private String phone;
	
	private boolean withdrawn;
	private LocalDateTime withdrawnAt;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
