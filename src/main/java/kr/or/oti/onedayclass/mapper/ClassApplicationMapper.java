package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.dto.MyPageApplicationDTO;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;

@Mapper
public interface ClassApplicationMapper {

    // 클래스 신청
    int insertApplication(ClassApplicationVO applicationVO);

    // 특정 회원의 특정 클래스 신청 내역 조회
    ClassApplicationVO selectApplication(
            @Param("memberId") Long memberId,
            @Param("classId") Long classId);

    // 특정 회원이 해당 클래스에 신청한 기록이 있는지 확인
    int countApplication(
            @Param("memberId") Long memberId,
            @Param("classId") Long classId);

    // 특정 클래스의 신청자 목록 조회
    List<ClassApplicationVO> selectApplicationsByClassId(
            Long classId);

    // 특정 회원이 신청한 클래스 목록 조회
    List<MyPageApplicationDTO> selectApplicationsByMemberId(
            Long memberId);

    // 신청 취소
    int cancelApplication(
            @Param("applicationId") Long applicationId,
            @Param("memberId") Long memberId);

    // 개설자가 신청 승인
    int approveApplication(Long applicationId);

    // 개설자가 신청 거절
    int rejectApplication(Long applicationId);

    // 수강 완료 처리
    int completeApplication(Long applicationId);

    // 신청 번호로 신청 내역 조회
    ClassApplicationVO selectApplicationById(
            Long applicationId);

    // 특정 클래스의 현재 승인 인원 조회
    int countApprovedApplications(
            Long classId);
    
    // 현재 참여 인원 조회
    int countCurrentParticipants(Long classId);
    
    int cancelApplicationsByMember(Long memberId);
}