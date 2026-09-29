package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.MemberVO;

@Mapper
public interface AdminMapper {

    /*
     * =========================================================
     * 대시보드 기본 통계
     * =========================================================
     */

    int countMembers();

    int countClasses();

    int countApplications();

    int countComments();

    int countReviews();


    /*
     * =========================================================
     * 상태별 통계
     * =========================================================
     */

    int countClassesByStatus(
            @Param("status") String status);

    int countApplicationsByStatus(
            @Param("status") String status);


    /*
     * =========================================================
     * 회원 관리
     * =========================================================
     */

    List<MemberVO> selectMembers(
            @Param("keyword") String keyword,
            @Param("withdrawn") Integer withdrawn,
            @Param("roleId") Integer roleId,
            @Param("offset") int offset,
            @Param("size") int size);


    int countMembersForAdmin(
            @Param("keyword") String keyword,
            @Param("withdrawn") Integer withdrawn,
            @Param("roleId") Integer roleId);


    MemberVO selectMemberById(
            @Param("memberId") Long memberId);


    int updateMemberRole(
            @Param("memberId") Long memberId,
            @Param("roleId") Integer roleId);


    /*
     * =========================================================
     * 클래스 관리
     * =========================================================
     */

    List<ClassVO> selectClasses(
            @Param("keyword") String keyword,
            @Param("status") String status,
            @Param("offset") int offset,
            @Param("size") int size);


    int countClassesForAdmin(
            @Param("keyword") String keyword,
            @Param("status") String status);


    ClassVO selectClassById(
            @Param("classId") Long classId);


    int updateClassStatus(
            @Param("classId") Long classId,
            @Param("status") String status);


    /*
     * =========================================================
     * 클래스 강제 삭제
     * =========================================================
     */

    int deleteReviewsByClassId(
            @Param("classId") Long classId);

    int deleteCommentsByClassId(
            @Param("classId") Long classId);

    int deleteApplicationsByClassId(
            @Param("classId") Long classId);

    int deleteImagesByClassId(
            @Param("classId") Long classId);

    int deleteClassById(
            @Param("classId") Long classId);
}