package kr.or.oti.onedayclass.service;

import java.util.List;

import kr.or.oti.onedayclass.dto.ClassRegisterDTO;
import kr.or.oti.onedayclass.dto.ClassSearchDTO;
import kr.or.oti.onedayclass.dto.ClassUpdateDTO;
import kr.or.oti.onedayclass.vo.ClassVO;

public interface ClassService {

    // 클래스 등록
    Long register(
            ClassRegisterDTO registerDTO,
            Long hostId
    );

    // 클래스 전체 목록 조회
    List<ClassVO> getClassList();

    // 검색 + 페이징 클래스 목록 조회
    List<ClassVO> getClassList(
            ClassSearchDTO searchDTO
    );

    // 내가 개설한 클래스 목록 조회
    List<ClassVO> getClassesByHostId(
            Long hostId
    );

    // 클래스 상세 조회
    ClassVO getClass(Long classId);

    // 클래스 수정
    void updateClass(
            ClassUpdateDTO updateDTO,
            Long loginMemberId
    );

    // 클래스 삭제
    void deleteClass(
            Long classId,
            Long loginMemberId
    );
}