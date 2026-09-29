package kr.or.oti.onedayclass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.oti.onedayclass.mapper.MemberMapper;
import kr.or.oti.onedayclass.vo.MemberVO;

@SpringBootTest
class MemberMapperTest {

    @Autowired
    private MemberMapper memberMapper;

    @Test
    void insertMemberTest() {

        // ROLE_USER의 PK 조회
        Integer roleId =
                memberMapper.selectRoleIdByRoleName("ROLE_USER");

        assertNotNull(roleId);

        // 회원 정보 객체 생성
        MemberVO member = new MemberVO();

        member.setRoleId(roleId);
        member.setLoginId("user2");
        member.setPassword("1234");
        member.setName("테스트회원2");
        member.setNickname("user2");
        member.setEmail("user2@test.com");
        member.setPhone("010-2222-2222");

        int result = memberMapper.insertMember(member);

        assertEquals(1, result);
        assertNotNull(member.getMemberId());

        System.out.println("INSERT 결과 = " + result);
        System.out.println("생성된 memberId = " + member.getMemberId());
    }
}
