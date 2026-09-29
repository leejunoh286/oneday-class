package kr.or.oti.onedayclass.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.onedayclass.mapper.ClassImageMapper;
import kr.or.oti.onedayclass.vo.ClassImageVO;

@Service
public class ClassImageServiceImpl
        implements ClassImageService {

    private final ClassImageMapper classImageMapper;

    private final S3Service s3Service;


    public ClassImageServiceImpl(
            ClassImageMapper classImageMapper,
            S3Service s3Service) {

        this.classImageMapper =
                classImageMapper;

        this.s3Service =
                s3Service;
    }


    /*
     * =========================================================
     * 클래스 이미지 여러 개 업로드
     * =========================================================
     *
     * 신규 클래스 등록 시 사용
     *
     * 첫 번째 이미지:
     * sort_order = 0
     * is_thumbnail = true
     */
    @Override
    @Transactional
    public void uploadClassImages(
            Long classId,
            List<MultipartFile> files) {


        /*
         * 업로드 파일이 없으면
         * 아무것도 하지 않는다.
         */
        if (files == null
                || files.isEmpty()) {

            return;
        }


        /*
         * 실제 파일이 들어있는 것만 추려낸다.
         */
        List<MultipartFile> validFiles =
                new ArrayList<>();


        for (MultipartFile file : files) {

            if (file != null
                    && !file.isEmpty()) {

                validFiles.add(
                        file);
            }
        }


        /*
         * 실제 파일이 하나도 없으면 종료
         */
        if (validFiles.isEmpty()) {

            return;
        }


        /*
         * 이미지 최대 개수
         *
         * 현재 클래스당 최대 5장
         */
        if (validFiles.size() > 5) {

            throw new IllegalArgumentException(
                    "클래스 이미지는 최대 5장까지 등록할 수 있습니다.");
        }


        /*
         * 신규 등록이므로
         * 이미지 순서는 0부터 시작
         */
        int sortOrder =
                0;


        /*
         * 업로드된 S3 key를 임시 저장
         *
         * DB 작업 중 오류가 발생할 경우
         * 이미 S3에 올라간 파일을 삭제하기 위해 사용한다.
         */
        List<String> uploadedKeys =
                new ArrayList<>();


        try {

            for (MultipartFile file
                    : validFiles) {


                /*
                 * S3 실제 업로드
                 *
                 * 예:
                 *
                 * class-images/UUID.jpg
                 */
                String objectKey =
                        s3Service.uploadFile(
                                file,
                                "class-images");


                /*
                 * DB 저장 실패 시
                 * S3 정리를 위해 key 보관
                 */
                uploadedKeys.add(
                        objectKey);


                /*
                 * 첫 번째 이미지가 대표 이미지
                 */
                boolean thumbnail =
                        sortOrder == 0;


                /*
                 * DB 저장용 객체 생성
                 */
                ClassImageVO image =
                        ClassImageVO.builder()

                            .classId(
                                    classId)

                            /*
                             * image_url 컬럼에는
                             * 전체 URL이 아니라
                             * S3 object key를 저장
                             */
                            .imageUrl(
                                    objectKey)

                            .sortOrder(
                                    sortOrder)

                            .thumbnail(
                                    thumbnail)

                            .build();


                /*
                 * CLASS_IMAGE INSERT
                 */
                classImageMapper
                        .insertClassImage(
                                image);


                /*
                 * 다음 이미지 순서
                 */
                sortOrder++;
            }


        } catch (RuntimeException e) {


            /*
             * S3 업로드는 성공했는데
             * DB INSERT 중 실패했다면
             *
             * S3에 고아 파일이 남지 않도록 삭제
             */
            for (String objectKey
                    : uploadedKeys) {

                try {

                    s3Service.deleteFile(
                            objectKey);

                } catch (Exception ignored) {

                    /*
                     * S3 삭제 중 추가 오류가 나더라도
                     * 원래 발생한 예외를 유지한다.
                     */
                }
            }


            throw e;
        }
    }


    /*
     * =========================================================
     * 기존 클래스에 새 이미지 추가
     * =========================================================
     *
     * 클래스 수정 페이지에서 사용
     *
     * 기존 이미지의 마지막 sort_order 다음 번호부터
     * 새 이미지를 등록한다.
     */
    @Override
    @Transactional
    public void addClassImages(
            Long classId,
            List<MultipartFile> files) {


        /*
         * 전달된 파일 목록이 없으면 종료
         */
        if (files == null
                || files.isEmpty()) {

            return;
        }


        /*
         * 실제 파일이 있는 것만 추출
         */
        List<MultipartFile> validFiles =
                new ArrayList<>();


        for (MultipartFile file : files) {

            if (file != null
                    && !file.isEmpty()) {

                validFiles.add(
                        file);
            }
        }


        /*
         * 실제 선택한 파일이 없으면 종료
         */
        if (validFiles.isEmpty()) {

            return;
        }


        /*
         * 현재 DB에 저장된 이미지 개수
         */
        int currentCount =
                classImageMapper
                    .countImagesByClassId(
                            classId);


        /*
         * 기존 이미지 + 새 이미지가
         * 5장을 넘으면 등록 불가
         */
        if (currentCount
                + validFiles.size()
                > 5) {

            throw new IllegalArgumentException(
                    "클래스 이미지는 최대 5장까지 등록할 수 있습니다.");
        }


        /*
         * 다음에 사용할 sort_order
         *
         * 예:
         *
         * 기존:
         * 0, 1, 2
         *
         * 반환:
         * 3
         */
        int sortOrder =
                classImageMapper
                    .selectNextSortOrder(
                            classId);


        /*
         * 이번 요청에서 S3에 업로드된 key들
         *
         * DB 저장 실패 시 정리용
         */
        List<String> uploadedKeys =
                new ArrayList<>();


        try {

            for (MultipartFile file
                    : validFiles) {


                /*
                 * S3 업로드
                 */
                String objectKey =
                        s3Service.uploadFile(
                                file,
                                "class-images");


                uploadedKeys.add(
                        objectKey);


                /*
                 * 기존 이미지가 한 장도 없었다면
                 * 첫 번째 새 이미지를 대표 이미지로 지정
                 *
                 * 기존 이미지가 있다면
                 * 기존 대표 이미지를 유지
                 */
                boolean thumbnail =
                        currentCount == 0
                        && sortOrder == 0;


                ClassImageVO image =
                        ClassImageVO.builder()

                            .classId(
                                    classId)

                            .imageUrl(
                                    objectKey)

                            .sortOrder(
                                    sortOrder)

                            .thumbnail(
                                    thumbnail)

                            .build();


                /*
                 * DB 저장
                 */
                classImageMapper
                        .insertClassImage(
                                image);


                sortOrder++;
            }


        } catch (RuntimeException e) {


            /*
             * DB 작업 중 오류가 발생했다면
             * 이번 요청에서 업로드한 S3 파일들을 삭제
             */
            for (String objectKey
                    : uploadedKeys) {

                try {

                    s3Service.deleteFile(
                            objectKey);

                } catch (Exception ignored) {

                }
            }


            throw e;
        }
    }


    /*
     * =========================================================
     * 수정 페이지에서 선택한 이미지 여러 개 삭제
     * =========================================================
     */
    @Override
    @Transactional
    public void deleteImages(
            Long classId,
            List<Long> imageIds) {


        /*
         * 삭제할 이미지가 없으면 종료
         */
        if (imageIds == null
                || imageIds.isEmpty()) {

            return;
        }
        
        
        /* 삭제 전에 현재 대표 이미지가 삭제 대상인지 기억해둔다 */
        boolean thumbnailDeleted = false;
        

        for (Long imageId
                : imageIds) {


            /*
             * 이미지 DB 조회
             */
            ClassImageVO image =
                    classImageMapper
                        .selectImageById(
                                imageId);


            /*
             * 이미 존재하지 않는 이미지면 무시
             */
            if (image == null) {

                continue;
            }


            /*
             * 현재 수정 중인 클래스의 이미지인지 검사
             *
             * 예:
             *
             * URL 조작 등으로 다른 클래스의
             * imageId가 전달되더라도 삭제되지 않게 한다.
             */
            if (!classId.equals(
                    image.getClassId())) {

                continue;
            }
            
            
            /* 대표 이미지가 삭제되는지 확인 */
            if (Boolean.TRUE.equals(
                    image.getThumbnail())) {

                thumbnailDeleted =
                        true;
            }


            /*
             * S3 실제 파일 삭제
             */
            s3Service.deleteFile(
                    image.getImageUrl());


            /*
             * CLASS_IMAGE DB 삭제
             */
            classImageMapper
                    .deleteImage(
                            imageId);
        }
        
        
        /* 대표 이미지가 삭제됐다면 남은 이미지 중 첫 번째를 새 대표 이미지로 지정한다 */
        if (thumbnailDeleted) {


            List<ClassImageVO> remainingImages =
                    classImageMapper
                        .selectImagesByClassId(
                                classId);


            if (remainingImages != null
                    && !remainingImages.isEmpty()) {


                /*
                 * 혹시 대표 이미지가 남아있는 상황을 방지
                 */
                classImageMapper
                        .clearThumbnail(
                                classId);


                /*
                 * sort_order가 가장 작은
                 * 첫 번째 이미지를 대표로 지정
                 */
                ClassImageVO newThumbnail =
                        remainingImages.get(0);


                classImageMapper
                        .setThumbnail(
                                newThumbnail.getImageId());
            }
        }
    }


    /*
     * =========================================================
     * 이미지 목록 조회
     * =========================================================
     */
    @Override
    public List<ClassImageVO> getImagesByClassId(
            Long classId) {


        /*
         * DB에서 이미지 목록 조회
         */
        List<ClassImageVO> images =
                classImageMapper
                    .selectImagesByClassId(
                            classId);


        /*
         * DB에는 private S3 object key만 있으므로
         * 화면 출력용 Presigned URL을 생성한다.
         */
        for (ClassImageVO image
                : images) {


            String presignedUrl =
                    s3Service
                        .createPresignedGetUrl(
                                image.getImageUrl());


            image.setPresignedUrl(
                    presignedUrl);
        }


        return images;
    }


    /*
     * =========================================================
     * 대표 이미지 조회
     * =========================================================
     */
    @Override
    public ClassImageVO getThumbnail(
            Long classId) {


        ClassImageVO image =
                classImageMapper
                    .selectThumbnailByClassId(
                            classId);


        if (image == null) {

            return null;
        }


        /*
         * 화면에서 사용할 임시 S3 URL 생성
         */
        image.setPresignedUrl(

                s3Service
                    .createPresignedGetUrl(
                            image.getImageUrl())
        );


        return image;
    }
    
    
    @Override
    @Transactional
    public void changeThumbnail(
            Long classId,
            Long imageId) {

        /*
         * 대표 이미지 변경 요청이 없으면 종료
         */
        if (imageId == null) {
            return;
        }


        /*
         * 선택한 이미지 조회
         */
        ClassImageVO image =
                classImageMapper
                    .selectImageById(
                            imageId);


        if (image == null) {
            return;
        }


        /*
         * 다른 클래스의 이미지를
         * 대표 이미지로 지정하는 것 방지
         */
        if (!classId.equals(
                image.getClassId())) {

            throw new IllegalArgumentException(
                    "잘못된 이미지입니다.");
        }


        /*
         * 기존 대표 이미지 해제
         */
        classImageMapper
                .clearThumbnail(
                        classId);


        /*
         * 선택한 이미지를 대표 이미지로 지정
         */
        classImageMapper
                .setThumbnail(
                        imageId);
    }


    /*
     * =========================================================
     * 이미지 하나 삭제
     * =========================================================
     */
    @Override
    @Transactional
    public void deleteImage(
            Long imageId) {


        /*
         * 삭제하기 전에
         * S3 key를 알아야 하므로 먼저 조회
         */
        ClassImageVO image =
                classImageMapper
                    .selectImageById(
                            imageId);


        if (image == null) {

            return;
        }


        /*
         * 먼저 S3에서 실제 파일 삭제
         */
        s3Service.deleteFile(
                image.getImageUrl());


        /*
         * 이후 DB 데이터 삭제
         */
        classImageMapper.deleteImage(
                imageId);
    }


    /*
     * =========================================================
     * 클래스 이미지 전체 삭제
     * =========================================================
     */
    @Override
    @Transactional
    public void deleteAllImagesByClassId(
            Long classId) {


        /*
         * S3 파일을 지우기 위해
         * DB 이미지 목록을 먼저 가져온다.
         */
        List<ClassImageVO> images =
                classImageMapper
                    .selectImagesByClassId(
                            classId);


        /*
         * 실제 S3 파일 삭제
         */
        for (ClassImageVO image
                : images) {

            s3Service.deleteFile(
                    image.getImageUrl());
        }


        /*
         * DB 데이터 삭제
         *
         * 클래스 자체가 삭제될 때는
         * FK ON DELETE CASCADE로 자동 삭제되지만,
         *
         * S3 파일은 DB FK가 삭제해줄 수 없기 때문에
         * 클래스 삭제 전에 이 메서드를 호출하는 방식으로 사용한다.
         */
        classImageMapper
                .deleteImagesByClassId(
                        classId);
    }
}