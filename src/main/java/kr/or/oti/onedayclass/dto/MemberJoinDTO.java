package kr.or.oti.onedayclass.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class MemberJoinDTO {
	
	@NotBlank(message = "아이디를 입력해주세요.")
	@Size(min = 4, max = 50,
		  message = "아이디는 4자 이상 50자 이하로 입력해주세요.")
	private String loginId;
	
	@NotBlank(message = "비밀번호를 입력해주세요.")
	@Size(min = 8, max = 50,
		  message = "비밀번호는 8자 이상 50자 이하로 입력해주세요.")
	private String password;
	
	@NotBlank(message = "이름을 입력해주세요.")
	@Size(max = 50,
		  message = "이름은 50자 이하로 입력해주세요.")
	private String name;
	
	@NotBlank(message = "닉네임을 입력해주세요.")
	@Size(min = 2, max = 50,
		  message = "닉네임은 2자 이상 50자 이하로 입력해주세요.")
	private String nickname;
	
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식으로 입력해주세요.")
	@Size(max = 100,
		  message = "이메일은 100자 리이하로 ㅈ 입력해주세요.")
	private String email;
	
	@Pattern(
		regexp = "^$|^[0-9-]{10,20}$",
		message = "전화번호 형식이 올바르지 않습니다."
	)
	private String phone;
}









