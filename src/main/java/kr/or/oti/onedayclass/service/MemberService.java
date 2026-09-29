package kr.or.oti.onedayclass.service;

import kr.or.oti.onedayclass.dto.MemberJoinDTO;
import kr.or.oti.onedayclass.dto.MemberUpdateDTO;
import kr.or.oti.onedayclass.dto.PasswordChangeDTO;
import kr.or.oti.onedayclass.vo.MemberVO;

public interface MemberService {

    // 회원가입
    Long join(MemberJoinDTO joinDTO);


    // 회원 ID로 조회
    MemberVO getMemberById(Long memberId);


    // 회원정보 수정
    void updateMember(
            Long memberId,
            MemberUpdateDTO updateDTO);


    // 비밀번호 변경
    void changePassword(
            Long memberId,
            PasswordChangeDTO passwordChangeDTO);


    // 회원탈퇴
    void withdraw(Long memberId);
}