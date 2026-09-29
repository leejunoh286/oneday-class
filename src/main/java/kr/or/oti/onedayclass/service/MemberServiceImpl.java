package kr.or.oti.onedayclass.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.MemberJoinDTO;
import kr.or.oti.onedayclass.dto.MemberUpdateDTO;
import kr.or.oti.onedayclass.dto.PasswordChangeDTO;
import kr.or.oti.onedayclass.exception.DuplicateMemberException;
import kr.or.oti.onedayclass.mapper.ClassApplicationMapper;
import kr.or.oti.onedayclass.mapper.ClassMapper;
import kr.or.oti.onedayclass.mapper.MemberMapper;
import kr.or.oti.onedayclass.vo.MemberVO;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberMapper memberMapper;
    private final ClassMapper classMapper;
    private final ClassApplicationMapper classApplicationMapper;
    private final BCryptPasswordEncoder passwordEncoder;


    public MemberServiceImpl(
            MemberMapper memberMapper,
            ClassMapper classMapper,
            ClassApplicationMapper classApplicationMapper,
            BCryptPasswordEncoder passwordEncoder) {

        this.memberMapper = memberMapper;
        this.classMapper = classMapper;
        this.classApplicationMapper = classApplicationMapper;
        this.passwordEncoder = passwordEncoder;
    }


    /*
     * =========================================================
     * 회원가입
     * =========================================================
     */
    @Override
    @Transactional
    public Long join(MemberJoinDTO joinDTO) {

        // 아이디 중복 검사
        if (memberMapper.countByLoginId(
                joinDTO.getLoginId()) > 0) {

            throw new DuplicateMemberException(
                    "loginId",
                    "이미 사용 중인 아이디입니다."
            );
        }


        // 닉네임 중복 검사
        if (memberMapper.countByNickname(
                joinDTO.getNickname()) > 0) {

            throw new DuplicateMemberException(
                    "nickname",
                    "이미 사용 중인 닉네임입니다."
            );
        }


        // 이메일 중복 검사
        if (memberMapper.countByEmail(
                joinDTO.getEmail()) > 0) {

            throw new DuplicateMemberException(
                    "email",
                    "이미 사용 중인 이메일입니다."
            );
        }


        // ROLE_USER 권한 PK 조회
        Integer roleId =
                memberMapper.selectRoleIdByRoleName(
                        "ROLE_USER"
                );


        if (roleId == null) {

            throw new IllegalStateException(
                    "ROLE_USER 권한이 DB에 존재하지 않습니다."
            );
        }


        // 회원 객체 생성
        MemberVO member =
                new MemberVO();


        member.setRoleId(
                roleId
        );


        member.setLoginId(
                joinDTO.getLoginId()
        );


        // 비밀번호 BCrypt 암호화
        member.setPassword(
                passwordEncoder.encode(
                        joinDTO.getPassword()
                )
        );


        member.setName(
                joinDTO.getName()
        );


        member.setNickname(
                joinDTO.getNickname()
        );


        member.setEmail(
                joinDTO.getEmail()
        );


        member.setPhone(
                joinDTO.getPhone()
        );


        // 회원 INSERT
        int result =
                memberMapper.insertMember(
                        member
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "회원가입 처리 중 오류가 발생했습니다."
            );
        }


        return member.getMemberId();
    }


    /*
     * =========================================================
     * 회원 ID로 조회
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public MemberVO getMemberById(
            Long memberId) {

        MemberVO member =
                memberMapper.selectByMemberId(
                        memberId
                );


        if (member == null) {

            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }


        return member;
    }


    /*
     * =========================================================
     * 회원정보 수정
     * =========================================================
     */
    @Override
    @Transactional
    public void updateMember(
            Long memberId,
            MemberUpdateDTO updateDTO) {

        /*
         * 현재 회원정보 조회
         */
        MemberVO currentMember =
                memberMapper.selectByMemberId(
                        memberId
                );


        if (currentMember == null) {

            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }


        /*
         * 닉네임이 실제로 변경된 경우에만
         * 중복 검사
         */
        if (!currentMember
                .getNickname()
                .equals(
                        updateDTO.getNickname()
                )) {


            if (memberMapper.countByNickname(
                    updateDTO.getNickname()) > 0) {


                throw new DuplicateMemberException(
                        "nickname",
                        "이미 사용 중인 닉네임입니다."
                );
            }
        }


        /*
         * 이메일이 실제로 변경된 경우에만
         * 중복 검사
         */
        if (!currentMember
                .getEmail()
                .equals(
                        updateDTO.getEmail()
                )) {


            if (memberMapper.countByEmail(
                    updateDTO.getEmail()) > 0) {


                throw new DuplicateMemberException(
                        "email",
                        "이미 사용 중인 이메일입니다."
                );
            }
        }


        /*
         * 수정값 적용
         */
        currentMember.setName(
                updateDTO.getName()
        );


        currentMember.setNickname(
                updateDTO.getNickname()
        );


        currentMember.setEmail(
                updateDTO.getEmail()
        );


        currentMember.setPhone(
                updateDTO.getPhone()
        );


        /*
         * DB UPDATE
         */
        int result =
                memberMapper.updateMember(
                        currentMember
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "회원정보 수정 중 오류가 발생했습니다."
            );
        }
    }


    /*
     * =========================================================
     * 비밀번호 변경
     * =========================================================
     */
    @Override
    @Transactional
    public void changePassword(
            Long memberId,
            PasswordChangeDTO passwordChangeDTO) {

        /*
         * 1. 현재 회원정보 조회
         */
        MemberVO member =
                memberMapper.selectByMemberId(
                        memberId
                );


        if (member == null) {

            throw new IllegalArgumentException(
                    "회원 정보를 찾을 수 없습니다."
            );
        }


        /*
         * 2. 현재 비밀번호 확인
         *
         * 사용자가 입력한 평문 비밀번호와
         * DB에 저장된 BCrypt 비밀번호를 비교한다.
         */
        boolean passwordMatches =
                passwordEncoder.matches(
                        passwordChangeDTO
                                .getCurrentPassword(),

                        member.getPassword()
                );


        if (!passwordMatches) {

            throw new IllegalArgumentException(
                    "현재 비밀번호가 일치하지 않습니다."
            );
        }


        /*
         * 3. 새 비밀번호와
         * 새 비밀번호 확인이 같은지 검사
         */
        if (!passwordChangeDTO
                .getNewPassword()
                .equals(
                        passwordChangeDTO
                                .getConfirmPassword()
                )) {

            throw new IllegalArgumentException(
                    "새 비밀번호와 비밀번호 확인이 일치하지 않습니다."
            );
        }


        /*
         * 4. 현재 비밀번호와
         * 새 비밀번호가 같은지 검사
         */
        if (passwordEncoder.matches(
                passwordChangeDTO.getNewPassword(),
                member.getPassword())) {

            throw new IllegalArgumentException(
                    "현재 비밀번호와 다른 비밀번호를 입력해주세요."
            );
        }


        /*
         * 5. 새 비밀번호 BCrypt 암호화
         */
        String encodedPassword =
                passwordEncoder.encode(
                        passwordChangeDTO
                                .getNewPassword()
                );


        /*
         * 6. DB 비밀번호 수정
         */
        int result =
                memberMapper.updatePassword(
                        memberId,
                        encodedPassword
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "비밀번호 변경 중 오류가 발생했습니다."
            );
        }
    }


    /*
     * =========================================================
     * 회원탈퇴
     * =========================================================
     */
    @Override
    @Transactional
    public void withdraw(
            Long memberId) {

        /*
         * 1. 운영 중인 클래스 검사
         *
         * RECRUITING
         * CLOSED
         *
         * 클래스가 하나라도 있으면 탈퇴 불가
         */
        int activeHostedClassCount =
                classMapper
                    .countActiveHostedClasses(
                            memberId
                    );


        if (activeHostedClassCount > 0) {

            throw new IllegalStateException(
                    "진행 중인 클래스가 있어 탈퇴할 수 없습니다."
            );
        }


        /*
         * 2. 진행 중 신청 취소
         *
         * PENDING
         * APPROVED
         *
         * → CANCELLED
         */
        classApplicationMapper
                .cancelApplicationsByMember(
                        memberId
                );


        /*
         * 3. 회원 논리 삭제
         *
         * 개인정보 익명화
         * withdrawn = 1
         */
        int result =
                memberMapper.withdrawMember(
                        memberId
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "회원탈퇴 처리 중 오류가 발생했습니다."
            );
        }
    }
}