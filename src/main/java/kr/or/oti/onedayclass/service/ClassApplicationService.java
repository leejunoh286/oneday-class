package kr.or.oti.onedayclass.service;

import java.util.List;

import kr.or.oti.onedayclass.dto.MyPageApplicationDTO;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;

public interface ClassApplicationService {

    // 클래스 신청
    Long apply(
            Long classId,
            Long memberId);

    // 현재 로그인 회원의 해당 클래스 신청 정보 조회
    ClassApplicationVO getApplication(
            Long classId,
            Long memberId);

    // 클래스 신청 취소
    void cancel(
            Long applicationId,
            Long memberId);

    // 개설자가 해당 클래스의 신청자 목록 조회
    List<ClassApplicationVO> getApplicationsByClass(
            Long classId,
            Long hostMemberId);

    // 마이페이지 - 내가 신청한 클래스 목록 조회
    List<MyPageApplicationDTO> getApplicationsByMemberId(
            Long memberId);

    // 개설자가 신청 승인
    void approve(
            Long applicationId,
            Long hostMemberId);

    // 개설자가 신청 거절
    void reject(
            Long applicationId,
            Long hostMemberId);

    // 개설자가 수강 완료 처리
    void complete(
            Long applicationId,
            Long hostMemberId);
    
    // 현재 참여 인원 조회
    int getCurrentParticipantCount(Long classId);
}