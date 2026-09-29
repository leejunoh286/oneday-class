package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.vo.ClassImageVO;

@Mapper
public interface ClassImageMapper {

    /*
     * 이미지 1개 DB 등록
     */
    int insertClassImage(
            ClassImageVO image);
    
    
    int selectNextSortOrder(
            Long classId);

    int countImagesByClassId(
            Long classId);


    /*
     * 특정 클래스의 이미지 전체 조회
     *
     * sort_order 순으로 조회한다.
     */
    List<ClassImageVO> selectImagesByClassId(
            Long classId);


    /*
     * 이미지 PK로 한 개 조회
     */
    ClassImageVO selectImageById(
            Long imageId);


    /*
     * 특정 클래스의 대표 이미지 조회
     */
    ClassImageVO selectThumbnailByClassId(
            Long classId);


    /*
     * 이미지 1개 삭제
     */
    int deleteImage(
            @Param("imageId")
            Long imageId);


    /*
     * 특정 클래스의 이미지 DB 데이터 전체 삭제
     *
     * DB FK ON DELETE CASCADE가 있기 때문에
     * 클래스 삭제 시 자동 삭제되지만,
     * S3 파일 삭제를 위해 조회 후 사용 가능하다.
     */
    int deleteImagesByClassId(
            @Param("classId")
            Long classId);
    
    
    int clearThumbnail(
            Long classId);

    int setThumbnail(
            Long imageId);
}