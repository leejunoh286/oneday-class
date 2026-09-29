package kr.or.oti.onedayclass.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    /*
     * 파일을 S3에 업로드하고
     * 저장된 S3 object key를 반환한다.
     */
    String uploadFile(
            MultipartFile file,
            String directory);


    /*
     * S3 객체 삭제
     */
    void deleteFile(
            String objectKey);


    /*
     * private S3 객체를 화면에서 볼 수 있도록
     * 임시 URL 생성
     */
    String createPresignedGetUrl(
            String objectKey);
}