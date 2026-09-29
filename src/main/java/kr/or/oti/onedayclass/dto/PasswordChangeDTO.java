package kr.or.oti.onedayclass.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class PasswordChangeDTO {
	
	@NotBlank(message = "현재 비밀번호를 입력해주세요.")
	private String currentPassword;
	
	@NotBlank(message = "새 비밀번호를 입력해주세요.")
	@Size(min = 8,
		  max = 30,
		  message = "새 비밀번호는 8자 이상 30자 이하로 입력해주세요."
	)
	private String newPassword;
	
	@NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
	private String confirmPassword;
}
