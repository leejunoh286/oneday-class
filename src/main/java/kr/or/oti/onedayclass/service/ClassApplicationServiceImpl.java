package kr.or.oti.onedayclass.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.MyPageApplicationDTO;
import kr.or.oti.onedayclass.exception.ClassApplicationException;
import kr.or.oti.onedayclass.mapper.ClassApplicationMapper;
import kr.or.oti.onedayclass.mapper.ClassMapper;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;
import kr.or.oti.onedayclass.vo.ClassVO;

@Service
public class ClassApplicationServiceImpl
        implements ClassApplicationService {

    private final ClassApplicationMapper classApplicationMapper;
    private final ClassMapper classMapper;

    public ClassApplicationServiceImpl(
            ClassApplicationMapper classApplicationMapper,
            ClassMapper classMapper) {

        this.classApplicationMapper = classApplicationMapper;
        this.classMapper = classMapper;
    }


    // 클래스 신청
    @Override
    @Transactional
    public Long apply(Long classId, Long memberId) {

        // 1. 클래스 존재 여부 확인
        ClassVO classVO =
                classMapper.selectClassById(classId);

        if (classVO == null) {
            throw new ClassApplicationException(
                    "존재하지 않는 클래스입니다.");
        }

        // 2. 모집 중인지 확인
        if (!"RECRUITING".equals(classVO.getStatus())) {
            throw new ClassApplicationException(
                    "현재 신청할 수 없는 클래스입니다.");
        }

        // 3. 신청 마감 여부 확인
        if (classVO.getDeadline() == null
                || !LocalDateTime.now()
                        .isBefore(classVO.getDeadline())) {

            throw new ClassApplicationException(
                    "클래스 신청이 마감되었습니다.");
        }

        // 4. 자기 자신이 개설한 클래스인지 확인
        if (classVO.getHostId().equals(memberId)) {
            throw new ClassApplicationException(
                    "본인이 개설한 클래스에는 신청할 수 없습니다.");
        }

        // 5. 기존 신청 기록 확인
        int applicationCount =
                classApplicationMapper.countApplication(
                        memberId,
                        classId);

        if (applicationCount > 0) {
            throw new ClassApplicationException(
                    "이미 신청한 클래스입니다.");
        }

        // 6. 신청 객체 생성
        ClassApplicationVO applicationVO =
                new ClassApplicationVO();

        applicationVO.setMemberId(memberId);
        applicationVO.setClassId(classId);
        applicationVO.setStatus("PENDING");

        // 7. DB INSERT
        int result =
                classApplicationMapper.insertApplication(
                        applicationVO);

        if (result != 1) {
            throw new ClassApplicationException(
                    "클래스 신청에 실패했습니다.");
        }

        return applicationVO.getApplicationId();
    }


    // 로그인 회원의 특정 클래스 신청 정보
    @Override
    @Transactional(readOnly = true)
    public ClassApplicationVO getApplication(
            Long classId,
            Long memberId) {

        return classApplicationMapper.selectApplication(
                memberId,
                classId);
    }


    // 신청 취소
    @Override
    @Transactional
    public void cancel(
            Long applicationId,
            Long memberId) {

        ClassApplicationVO application =
                classApplicationMapper
                        .selectApplicationById(applicationId);

        if (application == null) {
            throw new ClassApplicationException(
                    "존재하지 않는 신청입니다.");
        }

        // 본인의 신청인지 확인
        if (!application.getMemberId().equals(memberId)) {
            throw new ClassApplicationException(
                    "본인의 신청만 취소할 수 있습니다.");
        }

        // PENDING만 취소 가능
        if (!"PENDING".equals(application.getStatus())) {
            throw new ClassApplicationException(
                    "승인 대기 중인 신청만 취소할 수 있습니다.");
        }

        int result =
                classApplicationMapper.cancelApplication(
                        applicationId,
                        memberId);

        if (result != 1) {
            throw new ClassApplicationException(
                    "신청 취소에 실패했습니다.");
        }
    }


    // 개설자의 신청자 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ClassApplicationVO> getApplicationsByClass(
            Long classId,
            Long hostMemberId) {

        ClassVO classVO =
                classMapper.selectClassById(classId);

        checkHost(classVO, hostMemberId);

        return classApplicationMapper
                .selectApplicationsByClassId(classId);
    }


    // =========================================
    // 마이페이지 - 내가 신청한 클래스 목록 조회
    // =========================================

    @Override
    @Transactional
    public List<MyPageApplicationDTO> getApplicationsByMemberId(
            Long memberId) {

        /*
         * 마이페이지에서 classStatus도 같이 보여주므로
         * 먼저 모든 클래스의 상태를 현재 시간 기준으로 최신화한다.
         */
        classMapper.updateAllClassStatuses();

        return classApplicationMapper
                .selectApplicationsByMemberId(memberId);
    }


    // 신청 승인
    @Override
    @Transactional
    public void approve(
            Long applicationId,
            Long hostMemberId) {

        // 1. 승인하려는 신청 조회
        ClassApplicationVO application =
                getApplicationById(applicationId);

        // 2. 클래스 조회 + 행 잠금
        ClassVO classVO =
                classMapper.selectClassByIdForUpdate(
                        application.getClassId());

        // 3. 개설자 권한 확인
        checkHost(classVO, hostMemberId);

        // 4. PENDING 상태인지 확인
        if (!"PENDING".equals(application.getStatus())) {
            throw new ClassApplicationException(
                    "승인 대기 중인 신청만 승인할 수 있습니다.");
        }

        // 5. 현재 승인 인원 조회
        int approvedCount =
                classApplicationMapper
                        .countApprovedApplications(
                                application.getClassId());

        // 6. 정원 확인
        if (approvedCount >= classVO.getCapacity()) {
            throw new ClassApplicationException(
                    "클래스 정원이 가득 차서 더 이상 승인할 수 없습니다.");
        }

        // 7. 승인 처리
        int result =
                classApplicationMapper
                        .approveApplication(applicationId);

        if (result != 1) {
            throw new ClassApplicationException(
                    "신청 승인에 실패했습니다.");
        }
    }


    // 신청 거절
    @Override
    @Transactional
    public void reject(
            Long applicationId,
            Long hostMemberId) {

        ClassApplicationVO application =
                getApplicationById(applicationId);

        ClassVO classVO =
                classMapper.selectClassById(
                        application.getClassId());

        checkHost(classVO, hostMemberId);

        if (!"PENDING".equals(application.getStatus())) {
            throw new ClassApplicationException(
                    "승인 대기 중인 신청만 거절할 수 있습니다.");
        }

        int result =
                classApplicationMapper
                        .rejectApplication(applicationId);

        if (result != 1) {
            throw new ClassApplicationException(
                    "신청 거절에 실패했습니다.");
        }
    }


    // 수강 완료 처리
    @Override
    @Transactional
    public void complete(
            Long applicationId,
            Long hostMemberId) {

        // 1. 신청 조회
        ClassApplicationVO application =
                getApplicationById(applicationId);

        // 2. 해당 클래스 조회
        ClassVO classVO =
                classMapper.selectClassById(
                        application.getClassId());

        // 3. 클래스 개설자인지 확인
        checkHost(classVO, hostMemberId);

        // 4. APPROVED 상태인지 확인
        if (!"APPROVED".equals(application.getStatus())) {
            throw new ClassApplicationException(
                    "승인된 신청만 수강 완료 처리할 수 있습니다.");
        }

        // 5. 클래스 진행일이 지났는지 확인
        if (classVO.getClassDate() == null
                || LocalDateTime.now()
                        .isBefore(classVO.getClassDate())) {

            throw new ClassApplicationException(
                    "클래스 진행이 끝난 후에만 수강 완료 처리할 수 있습니다.");
        }

        // 6. 수강 완료 처리
        int result =
                classApplicationMapper
                        .completeApplication(applicationId);

        if (result != 1) {
            throw new ClassApplicationException(
                    "수강 완료 처리에 실패했습니다.");
        }
    }


    // 신청 번호로 신청 조회
    private ClassApplicationVO getApplicationById(
            Long applicationId) {

        ClassApplicationVO application =
                classApplicationMapper
                        .selectApplicationById(applicationId);

        if (application == null) {
            throw new ClassApplicationException(
                    "존재하지 않는 신청입니다.");
        }

        return application;
    }


    // 현재 로그인 회원이 해당 클래스의 개설자인지 검사
    private void checkHost(
            ClassVO classVO,
            Long hostMemberId) {

        if (classVO == null) {
            throw new ClassApplicationException(
                    "존재하지 않는 클래스입니다.");
        }

        if (!classVO.getHostId().equals(hostMemberId)) {
            throw new ClassApplicationException(
                    "클래스 개설자만 처리할 수 있습니다.");
        }
    }
    
    
    // 현재 참여 인원 조회
    @Override
    @Transactional(readOnly = true)
    public int getCurrentParticipantCount(Long classId) {

        return classApplicationMapper
                .countCurrentParticipants(classId);
    }
}