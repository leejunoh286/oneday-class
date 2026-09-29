package kr.or.oti.onedayclass.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.dto.MemberDTO;
import kr.or.oti.onedayclass.vo.MemberVO;

@Mapper
public interface MemberMapper {
	
	// 회원 저장
	int insertMember(MemberVO member);
	
	// 로그인 아이디로 회원 조회 
	MemberVO selectByLoginId(String loginId);
	
	// 회원 ID로 회원 조회
	MemberVO selectByMemberId(Long memberId);
	
	// 회원가입 중복 검사
	int countByLoginId(String loginId);
	
	int countByNickname(String nickname);
	
	int countByEmail(String email);
	
	// 권한 이름으로 ROLE PK 조회
	Integer selectRoleIdByRoleName(
			@Param("roleName") String roleName);
	
	// 권한 ID로 권한 이름 조회
	String selectRoleNameByRoleId(Integer roleId);
	
	// 회원정보 수정
	int updateMember(MemberVO member);
	
	int updatePassword(
			@Param("memberId") Long memberId,
			@Param("password") String password);
	
	// 회원탈퇴 처리
	int withdrawMember(Long memberId);
}
