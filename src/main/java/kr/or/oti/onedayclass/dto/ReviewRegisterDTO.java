package kr.or.oti.onedayclass.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.Data;

@Data
public class ReviewRegisterDTO {

    @NotNull(message = "평점을 선택해주세요.")
    @Min(value = 1, message = "평점은 1점 이상이어야 합니다.")
    @Max(value = 5, message = "평점은 5점 이하여야 합니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용을 입력해주세요.")
    @Size(max = 1000, message = "리뷰 내용은 1000자 이내로 입력해주세요.")
    private String content;
}
