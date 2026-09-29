package kr.or.oti.onedayclass.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.onedayclass.vo.ClassImageVO;

public interface ClassImageService {

    /*
     * 클래스 이미지 여러 개 업로드
     */
    void uploadClassImages(
            Long classId,
            List<MultipartFile> files);
    
    
    void addClassImages(
            Long classId,
            List<MultipartFile> files);

    void deleteImages(
            Long classId,
            List<Long> imageIds);


    /*
     * 클래스 이미지 목록 조회
     *
     * Presigned URL까지 만들어서 반환
     */
    List<ClassImageVO> getImagesByClassId(
            Long classId);


    /*
     * 대표 이미지 조회
     */
    ClassImageVO getThumbnail(
            Long classId);
    
    
    void changeThumbnail(
            Long classId,
            Long imageId);


    /*
     * 이미지 한 개 삭제
     */
    void deleteImage(
            Long imageId);


    /*
     * 클래스에 연결된 이미지 전체 삭제
     *
     * DB + S3 모두 삭제
     */
    void deleteAllImagesByClassId(
            Long classId);
}