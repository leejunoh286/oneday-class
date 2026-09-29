package kr.or.oti.onedayclass.service;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

@Service
public class S3ServiceImpl implements S3Service {

    /*
     * 실제 S3 업로드/삭제 담당
     */
    private final S3Client s3Client;


    /*
     * private 파일 조회용
     * 임시 URL 생성 담당
     */
    private final S3Presigner s3Presigner;


    /*
     * application.properties:
     *
     * aws.s3.bucket=activeon-class-images-2026-leejunoh
     */
    @Value("${aws.s3.bucket}")
    private String bucketName;


    public S3ServiceImpl(
            S3Client s3Client,
            S3Presigner s3Presigner) {

        this.s3Client =
                s3Client;

        this.s3Presigner =
                s3Presigner;
    }


    /*
     * =========================================================
     * S3 파일 업로드
     * =========================================================
     *
     * 예:
     *
     * 원본 파일:
     * tennis.jpg
     *
     * S3 저장:
     * class-images/UUID_tennis.jpg
     *
     * DB에는 전체 URL이 아닌
     * class-images/UUID_tennis.jpg
     * 라는 key를 저장한다.
     */
    @Override
    public String uploadFile(
            MultipartFile file,
            String directory) {

        /*
         * 파일이 없으면 업로드 불가
         */
        if (file == null
                || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "업로드할 파일이 없습니다.");
        }


        /*
         * 이미지 파일인지 검사
         */
        String contentType =
                file.getContentType();


        if (contentType == null
                || !contentType.startsWith("image/")) {

            throw new IllegalArgumentException(
                    "이미지 파일만 업로드할 수 있습니다.");
        }


        /*
         * 너무 큰 파일 업로드 방지
         *
         * 현재 최대 10MB
         */
        long maxFileSize =
                10L * 1024 * 1024;


        if (file.getSize()
                > maxFileSize) {

            throw new IllegalArgumentException(
                    "이미지는 최대 10MB까지 업로드할 수 있습니다.");
        }


        /*
         * 사용자가 올린 원본 파일명
         *
         * 예:
         * tennis.jpg
         */
        String originalFilename =
                file.getOriginalFilename();


        /*
         * 확장자 추출
         *
         * 예:
         * .jpg
         */
        String extension =
                getExtension(
                        originalFilename);


        /*
         * UUID를 사용해서
         * 같은 이름의 파일이 업로드되어도
         * 충돌하지 않게 만든다.
         *
         * 예:
         *
         * class-images/
         * a8f51...-....jpg
         */
        String objectKey =
                directory
                + "/"
                + UUID.randomUUID()
                + extension;


        try {

            /*
             * S3에 저장할 파일 정보
             */
            PutObjectRequest putRequest =
                    PutObjectRequest.builder()
                            .bucket(
                                    bucketName)

                            .key(
                                    objectKey)

                            /*
                             * 이미지 MIME 타입
                             *
                             * image/jpeg
                             * image/png
                             * 등
                             */
                            .contentType(
                                    contentType)

                            .build();


            /*
             * 실제 S3 업로드
             */
            s3Client.putObject(

                    putRequest,

                    RequestBody.fromInputStream(
                            file.getInputStream(),
                            file.getSize())
            );


            /*
             * DB에는 이 objectKey를 저장한다.
             */
            return objectKey;


        } catch (IOException e) {

            throw new RuntimeException(
                    "S3 파일 업로드 중 오류가 발생했습니다.",
                    e);
        }
    }


    /*
     * =========================================================
     * S3 파일 삭제
     * =========================================================
     */
    @Override
    public void deleteFile(
            String objectKey) {

        /*
         * 삭제할 key가 없으면
         * 아무 작업도 하지 않는다.
         */
        if (objectKey == null
                || objectKey.trim().isEmpty()) {

            return;
        }


        /*
         * 어떤 버킷의 어떤 파일을
         * 삭제할지 설정
         */
        DeleteObjectRequest request =
                DeleteObjectRequest.builder()
                        .bucket(
                                bucketName)

                        .key(
                                objectKey)

                        .build();


        /*
         * 실제 S3 객체 삭제
         */
        s3Client.deleteObject(
                request);
    }


    /*
     * =========================================================
     * Presigned URL 생성
     * =========================================================
     *
     * 현재 S3 버킷은 private이다.
     *
     * 따라서 브라우저가 그냥 S3 URL에
     * 접근하면 이미지를 볼 수 없다.
     *
     * 그래서 일정 시간 동안만 사용할 수 있는
     * 임시 URL을 생성한다.
     */
    @Override
    public String createPresignedGetUrl(
            String objectKey) {


        if (objectKey == null
                || objectKey.trim().isEmpty()) {

            return null;
        }


        /*
         * S3에서 조회할 객체 지정
         */
        GetObjectRequest getObjectRequest =
                GetObjectRequest.builder()
                        .bucket(
                                bucketName)

                        .key(
                                objectKey)

                        .build();


        /*
         * Presigned URL 설정
         *
         * 현재 유효시간 1시간
         */
        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()

                        .signatureDuration(
                                Duration.ofHours(1))

                        .getObjectRequest(
                                getObjectRequest)

                        .build();


        /*
         * 실제 임시 URL 생성
         */
        return s3Presigner
                .presignGetObject(
                        presignRequest)

                .url()
                .toString();
    }


    /*
     * =========================================================
     * 파일 확장자 추출
     * =========================================================
     *
     * tennis.jpg
     * →
     * .jpg
     */
    private String getExtension(
            String filename) {


        /*
         * 파일명이 없으면
         * 확장자도 없음
         */
        if (filename == null
                || filename.trim().isEmpty()) {

            return "";
        }


        /*
         * 마지막 . 위치
         */
        int dotIndex =
                filename.lastIndexOf(".");


        /*
         * .이 없거나
         * 마지막 문자가 .이면
         * 확장자 없는 것으로 처리
         */
        if (dotIndex < 0
                || dotIndex
                    == filename.length() - 1) {

            return "";
        }


        /*
         * .jpg
         * .png
         * 등의 확장자를 반환
         */
        return filename
                .substring(
                        dotIndex)
                .toLowerCase();
    }
}