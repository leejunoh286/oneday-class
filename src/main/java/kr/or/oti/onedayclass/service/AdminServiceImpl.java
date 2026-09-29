package kr.or.oti.onedayclass.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.PageRequestDTO;
import kr.or.oti.onedayclass.mapper.AdminMapper;
import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.MemberVO;

@Service
public class AdminServiceImpl implements AdminService {

    private final AdminMapper adminMapper;

    /*
     * 관리자에서 클래스 삭제 시
     * S3 이미지까지 같이 삭제하기 위해 사용
     */
    private final ClassImageService classImageService;


    public AdminServiceImpl(
            AdminMapper adminMapper,
            ClassImageService classImageService) {

        this.adminMapper = adminMapper;
        this.classImageService = classImageService;
    }


    /*
     * =========================================================
     * 관리자 대시보드 통계
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public Map<String, Integer> getDashboardStats() {

        Map<String, Integer> stats =
                new HashMap<>();


        /*
         * 기본 통계
         */

        stats.put(
                "memberCount",
                adminMapper.countMembers()
        );

        stats.put(
                "classCount",
                adminMapper.countClasses()
        );

        stats.put(
                "applicationCount",
                adminMapper.countApplications()
        );

        stats.put(
                "commentCount",
                adminMapper.countComments()
        );

        stats.put(
                "reviewCount",
                adminMapper.countReviews()
        );


        /*
         * 클래스 상태별 통계
         */

        stats.put(
                "recruitingCount",
                adminMapper.countClassesByStatus(
                        "RECRUITING"
                )
        );

        stats.put(
                "closedCount",
                adminMapper.countClassesByStatus(
                        "CLOSED"
                )
        );

        stats.put(
                "completedClassCount",
                adminMapper.countClassesByStatus(
                        "COMPLETED"
                )
        );


        /*
         * 신청 상태별 통계
         */

        stats.put(
                "pendingCount",
                adminMapper.countApplicationsByStatus(
                        "PENDING"
                )
        );

        stats.put(
                "approvedCount",
                adminMapper.countApplicationsByStatus(
                        "APPROVED"
                )
        );

        stats.put(
                "rejectedCount",
                adminMapper.countApplicationsByStatus(
                        "REJECTED"
                )
        );

        stats.put(
                "cancelledCount",
                adminMapper.countApplicationsByStatus(
                        "CANCELLED"
                )
        );

        stats.put(
                "completedApplicationCount",
                adminMapper.countApplicationsByStatus(
                        "COMPLETED"
                )
        );


        return stats;
    }


    /*
     * =========================================================
     * 관리자 회원 목록
     * 검색 + 필터 + 페이징
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<MemberVO> getMembers(
            String keyword,
            Integer withdrawn,
            Integer roleId,
            PageRequestDTO pageRequestDTO) {


        /*
         * 검색어 앞뒤 공백 제거
         */
        if (keyword != null) {

            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }


        /*
         * PageRequestDTO에서
         * offset과 size를 가져옴
         *
         * page = 1 → offset = 0
         * page = 2 → offset = 10
         * page = 3 → offset = 20
         */
        int offset =
                pageRequestDTO.getOffset();

        int size =
                pageRequestDTO.getSize();


        return adminMapper.selectMembers(
                keyword,
                withdrawn,
                roleId,
                offset,
                size
        );
    }


    /*
     * =========================================================
     * 관리자 회원 검색 결과 전체 개수
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public int getMemberCount(
            String keyword,
            Integer withdrawn,
            Integer roleId) {


        if (keyword != null) {

            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }


        return adminMapper.countMembersForAdmin(
                keyword,
                withdrawn,
                roleId
        );
    }


    /*
     * =========================================================
     * 회원 권한 변경
     * =========================================================
     */
    @Override
    @Transactional
    public void changeMemberRole(
            Long loginAdminId,
            Long targetMemberId,
            Integer roleId) {


        /*
         * 자기 자신의 관리자 권한 변경 방지
         */
        if (loginAdminId.equals(
                targetMemberId)) {

            throw new IllegalStateException(
                    "자신의 관리자 권한은 변경할 수 없습니다."
            );
        }


        /*
         * 허용된 권한인지 확인
         *
         * 1 = ROLE_USER
         * 2 = ROLE_ADMIN
         */
        if (roleId == null
                || (roleId != 1
                && roleId != 2)) {

            throw new IllegalArgumentException(
                    "올바르지 않은 권한입니다."
            );
        }


        /*
         * 대상 회원 조회
         */
        MemberVO member =
                adminMapper.selectMemberById(
                        targetMemberId
                );


        if (member == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 회원입니다."
            );
        }


        /*
         * 탈퇴한 회원은 권한 변경 불가
         */
        if (member.isWithdrawn()) {

            throw new IllegalStateException(
                    "탈퇴한 회원의 권한은 변경할 수 없습니다."
            );
        }


        /*
         * 권한 변경
         */
        int result =
                adminMapper.updateMemberRole(
                        targetMemberId,
                        roleId
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "회원 권한 변경에 실패했습니다."
            );
        }
    }


    /*
     * =========================================================
     * 관리자 클래스 목록
     * 검색 + 상태 필터 + 페이징
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public List<ClassVO> getClasses(
            String keyword,
            String status,
            PageRequestDTO pageRequestDTO) {


        /*
         * 검색어 정리
         */
        if (keyword != null) {

            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }


        /*
         * 상태값 정리
         */
        if (status != null) {

            status = status.trim();

            if (status.isEmpty()) {
                status = null;
            }
        }


        /*
         * PageRequestDTO 사용
         */
        int offset =
                pageRequestDTO.getOffset();

        int size =
                pageRequestDTO.getSize();


        return adminMapper.selectClasses(
                keyword,
                status,
                offset,
                size
        );
    }


    /*
     * =========================================================
     * 관리자 클래스 검색 결과 전체 개수
     * =========================================================
     */
    @Override
    @Transactional(readOnly = true)
    public int getClassCount(
            String keyword,
            String status) {


        if (keyword != null) {

            keyword = keyword.trim();

            if (keyword.isEmpty()) {
                keyword = null;
            }
        }


        if (status != null) {

            status = status.trim();

            if (status.isEmpty()) {
                status = null;
            }
        }


        return adminMapper.countClassesForAdmin(
                keyword,
                status
        );
    }


    /*
     * =========================================================
     * 클래스 상태 변경
     * =========================================================
     */
    @Override
    @Transactional
    public void changeClassStatus(
            Long classId,
            String status) {


        /*
         * 클래스 존재 여부 확인
         */
        ClassVO classVO =
                adminMapper.selectClassById(
                        classId
                );


        if (classVO == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다."
            );
        }


        /*
         * 허용된 상태만 변경 가능
         */
        if (!"RECRUITING".equals(status)
                && !"CLOSED".equals(status)
                && !"COMPLETED".equals(status)) {

            throw new IllegalArgumentException(
                    "올바르지 않은 클래스 상태입니다."
            );
        }


        /*
         * 상태 변경
         */
        int result =
                adminMapper.updateClassStatus(
                        classId,
                        status
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "클래스 상태 변경에 실패했습니다."
            );
        }
    }


    /*
     * =========================================================
     * 관리자 클래스 강제 삭제
     * =========================================================
     */
    @Override
    @Transactional
    public void deleteClass(
            Long classId) {


        /*
         * 클래스가 존재하는지 먼저 확인
         */
        ClassVO classVO =
                adminMapper.selectClassById(
                        classId
                );


        if (classVO == null) {

            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다."
            );
        }


        /*
         * =====================================================
         * S3 이미지 삭제
         * =====================================================
         *
         * DB의 CLASS_IMAGE만 먼저 삭제하면
         * S3에 실제 파일이 남게 된다.
         *
         * 따라서 ClassImageService를 통해
         *
         * 1. CLASS_IMAGE 조회
         * 2. S3 실제 파일 삭제
         * 3. CLASS_IMAGE DB 데이터 삭제
         *
         * 순서로 처리한다.
         */
        classImageService
                .deleteAllImagesByClassId(
                        classId
                );


        /*
         * =====================================================
         * 나머지 자식 데이터 삭제
         * =====================================================
         */

        adminMapper.deleteReviewsByClassId(
                classId
        );

        adminMapper.deleteCommentsByClassId(
                classId
        );

        adminMapper.deleteApplicationsByClassId(
                classId
        );


        /*
         * =====================================================
         * 마지막으로 클래스 삭제
         * =====================================================
         */
        int result =
                adminMapper.deleteClassById(
                        classId
                );


        if (result != 1) {

            throw new IllegalStateException(
                    "클래스 삭제에 실패했습니다."
            );
        }
    }
}