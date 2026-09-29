package kr.or.oti.onedayclass.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import kr.or.oti.onedayclass.mapper.MemberMapper;
import kr.or.oti.onedayclass.vo.MemberVO;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberMapper memberMapper;

    public CustomUserDetailsService(MemberMapper memberMapper) {
        this.memberMapper = memberMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String loginId)
            throws UsernameNotFoundException {

        // 로그인 아이디로 회원 조회
        MemberVO member =
                memberMapper.selectByLoginId(loginId);

        // 존재하지 않는 회원
        if (member == null) {
            throw new UsernameNotFoundException(
                    "존재하지 않는 회원입니다."
            );
        }

        // 회원의 권한 이름 조회
        String roleName =
                memberMapper.selectRoleNameByRoleId(
                        member.getRoleId()
                );

        if (roleName == null) {
            throw new IllegalStateException(
                    "회원 권한 정보를 찾을 수 없습니다."
            );
        }

        // Spring Security에서 사용할 사용자 정보 반환
        return new CustomUserDetails(
                member,
                roleName
        );
    }
}
