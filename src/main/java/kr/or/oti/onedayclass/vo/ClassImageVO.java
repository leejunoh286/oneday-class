package kr.or.oti.onedayclass.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassImageVO {

    private Long imageId;

    private Long classId;

    /*
     * 컬럼명은 image_url이지만
     * 실제로는 S3 object key를 저장한다.
     *
     * 예:
     * class-images/UUID.jpg
     */
    private String imageUrl;

    /*
     * 이미지 출력 순서
     *
     * 0, 1, 2, ...
     */
    private Integer sortOrder;

    /*
     * 대표 이미지 여부
     *
     * true  = 대표 이미지
     * false = 일반 이미지
     */
    private Boolean thumbnail;

    private LocalDateTime createdAt;


    /*
     * 화면 출력용 Presigned URL
     *
     * DB 컬럼에는 없는 값이다.
     * 조회 후 Service에서 만들어서 넣어준다.
     */
    private String presignedUrl;
}