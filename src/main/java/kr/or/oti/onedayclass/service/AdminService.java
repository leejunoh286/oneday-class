package kr.or.oti.onedayclass.service;

import java.util.List;
import java.util.Map;

import kr.or.oti.onedayclass.dto.PageRequestDTO;
import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.MemberVO;

public interface AdminService {

    /*
     * =========================================================
     * 관리자 대시보드
     * =========================================================
     */

    Map<String, Integer> getDashboardStats();


    /*
     * =========================================================
     * 관리자 회원 관리
     * =========================================================
     */

    List<MemberVO> getMembers(
            String keyword,
            Integer withdrawn,
            Integer roleId,
            PageRequestDTO pageRequestDTO);


    int getMemberCount(
            String keyword,
            Integer withdrawn,
            Integer roleId);


    void changeMemberRole(
            Long loginAdminId,
            Long targetMemberId,
            Integer roleId);


    /*
     * =========================================================
     * 관리자 클래스 관리
     * =========================================================
     */

    List<ClassVO> getClasses(
            String keyword,
            String status,
            PageRequestDTO pageRequestDTO);


    int getClassCount(
            String keyword,
            String status);


    void changeClassStatus(
            Long classId,
            String status);


    void deleteClass(
            Long classId);
}