package kr.or.oti.onedayclass.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import kr.or.oti.onedayclass.vo.MemberVO;

public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    private final MemberVO member;
    private final String roleName;

    public CustomUserDetails(MemberVO member, String roleName) {
        this.member = member;
        this.roleName = roleName;
    }

    // 로그인한 회원의 PK
    public Long getMemberId() {
        return member.getMemberId();
    }

    // 로그인한 회원의 닉네임
    public String getNickname() {
        return member.getNickname();
    }

    // 회원 정보
    public MemberVO getMember() {
        return member;
    }

    // 회원 권한
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return Collections.singletonList(
                new SimpleGrantedAuthority(roleName)
        );
    }

    // DB에 저장된 BCrypt 비밀번호
    @Override
    public String getPassword() {
        return member.getPassword();
    }

    // 로그인 아이디
    @Override
    public String getUsername() {
        return member.getLoginId();
    }

    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠금 여부
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
