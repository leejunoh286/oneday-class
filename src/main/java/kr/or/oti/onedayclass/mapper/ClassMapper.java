package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import kr.or.oti.onedayclass.dto.ClassSearchDTO;
import kr.or.oti.onedayclass.vo.ClassVO;

@Mapper
public interface ClassMapper {

    // 클래스 등록
    int insertClass(ClassVO classVO);

    // 클래스 전체 목록 조회
    List<ClassVO> selectClassList();

    // 클래스 검색 + 페이징 목록 조회
    List<ClassVO> selectClassListBySearch(
            ClassSearchDTO searchDTO);

    // 클래스 검색 결과 전체 개수 조회
    int countClassListBySearch(
            ClassSearchDTO searchDTO);

    // 특정 회원이 개설한 클래스 목록 조회
    List<ClassVO> selectClassesByHostId(
            Long hostId);

    // 클래스 상세 조회
    ClassVO selectClassById(Long classId);

    // 클래스 승인 처리 시 행 잠금 조회
    ClassVO selectClassByIdForUpdate(Long classId);

    // 모든 클래스 상태 갱신
    int updateAllClassStatuses();

    // 특정 클래스 상태 갱신
    int updateClassStatus(Long classId);

    // 클래스 수정
    int updateClass(ClassVO classVO);

    // 클래스 삭제
    int deleteClass(Long classId);
    
    // 클래스 상태 자동 변경
    int closeExpiredRecruitingClasses();
    
    int completeExpiredClasses();
    
    int countActiveHostedClasses(Long memberId);
}









