package kr.or.oti.onedayclass.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class MemberUpdateDTO {
	@NotBlank(message = "이름을 입력해주세요.")
	@Size(max = 30, message = "이름은 30자 이하로 입력해주세요.")
	private String name;
	
	@NotBlank(message = "닉네임을 입력해주세요.")
	@Size(max = 30, message = "닉네임은 30자 이하로 입력해주세요.")
	private String nickname;
	
	@NotBlank(message = "이메일을 입력해주세요.")
	@Email(message = "올바른 이메일 형식이 아닙니다.")
	private String email;
	
	@Pattern(
		regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$",
		message = "올바른 전화번호 형식이 아닙니다."
	)
	private String phone;
}
