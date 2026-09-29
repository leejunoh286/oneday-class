package kr.or.oti.onedayclass.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Data
public class ClassUpdateDTO {

    @NotNull(message = "클래스 번호가 필요합니다.")
    private Long classId;


    @NotNull(message = "카테고리를 선택해주세요.")
    private Integer categoryId;


    @NotBlank(message = "클래스 제목을 입력해주세요.")
    @Size(max = 200, message = "클래스 제목은 200자 이하로 입력해주세요.")
    private String title;


    @NotBlank(message = "클래스 설명을 입력해주세요.")
    private String description;


    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
    private Integer price;


    @NotNull(message = "모집 인원을 입력해주세요.")
    @Min(value = 1, message = "모집 인원은 1명 이상이어야 합니다.")
    private Integer capacity;


    @NotNull(message = "클래스 진행 일시를 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime classDate;


    @NotNull(message = "모집 마감 일시를 입력해주세요.")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime deadline;


    @NotBlank(message = "장소를 입력해주세요.")
    @Size(max = 255, message = "장소는 255자 이하로 입력해주세요.")
    private String location;
}
