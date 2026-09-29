package kr.or.oti.onedayclass.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    // 비밀번호 암호화 객체
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Spring Security 설정
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // URL 접근 권한 설정
            .authorizeRequests()

                // 로그인하지 않아도 접근 가능
                .antMatchers(
                        "/",
                        "/member/join",
                        "/member/login",
                        "/class/list",
                        "/class/detail",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/map/**"
                ).permitAll()

                // 관리자만 접근 가능
                .antMatchers("/admin/**")
                .hasRole("ADMIN")

                // 나머지 요청은 로그인 필요
                .anyRequest()
                .authenticated()

            .and()

            // 로그인 설정
            .formLogin()

                // 우리가 만든 로그인 화면 사용
                .loginPage("/member/login")

                // 로그인 POST 요청을 Spring Security가 처리
                .loginProcessingUrl("/member/login")

                // 로그인 아이디 input의 name
                .usernameParameter("loginId")

                // 비밀번호 input의 name
                .passwordParameter("password")

                // 로그인 성공 후 메인 화면으로 이동
                .defaultSuccessUrl("/", true)

                // 로그인 실패
                .failureUrl("/member/login?error")

                .permitAll()

            .and()

            // 로그아웃 설정
            .logout()

                // POST /member/logout을 Spring Security가 처리
                .logoutUrl("/member/logout")

                // 로그아웃 성공 후 이동
                .logoutSuccessUrl("/member/login?logout")

                // 기존 세션 제거
                .invalidateHttpSession(true)

                // 인증 정보 제거
                .clearAuthentication(true)

                .permitAll();

        return http.build();
    }
}
