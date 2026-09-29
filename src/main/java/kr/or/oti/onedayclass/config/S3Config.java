package kr.or.oti.onedayclass.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    @Value("${aws.s3.region}")
    private String region;

    /*
     * 실제 S3에
     * 업로드 / 삭제 등의 요청을 보내는 객체
     */
    @Bean
    public S3Client s3Client() {

        return S3Client.builder()
                .region(Region.of(region))

                /*
                 * Windows 환경변수에 등록한
                 *
                 * AWS_ACCESS_KEY_ID
                 * AWS_SECRET_ACCESS_KEY
                 *
                 * 등을 자동으로 찾아서 사용한다.
                 */
                .credentialsProvider(
                        DefaultCredentialsProvider.create())

                .build();
    }


    /*
     * private S3 이미지를 브라우저에서
     * 일정 시간 볼 수 있도록
     * Presigned URL을 만드는 객체
     */
    @Bean
    public S3Presigner s3Presigner() {

        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(
                        DefaultCredentialsProvider.create())
                .build();
    }
}