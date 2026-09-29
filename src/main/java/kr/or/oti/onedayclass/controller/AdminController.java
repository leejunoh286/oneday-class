package kr.or.oti.onedayclass.controller;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.onedayclass.dto.PageRequestDTO;
import kr.or.oti.onedayclass.dto.PageResponseDTO;
import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.AdminService;
import kr.or.oti.onedayclass.service.MemberService;
import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.MemberVO;

@Controller
public class AdminController {

    private final AdminService adminService;
    private final MemberService memberService;


    public AdminController(
            AdminService adminService,
            MemberService memberService) {

        this.adminService = adminService;
        this.memberService = memberService;
    }


    /*
     * =========================================================
     * 관리자 대시보드
     * =========================================================
     */
    @GetMapping("/admin")
    public String adminHome(
            Model model) {

        Map<String, Integer> stats =
                adminService.getDashboardStats();


        model.addAttribute(
                "stats",
                stats
        );


        return "admin/dashboard";
    }


    /*
     * =========================================================
     * 관리자 회원 목록
     * 검색 + 필터 + 페이징
     * =========================================================
     */
    @GetMapping("/admin/member/list")
    public String memberList(

            @RequestParam(
                    value = "keyword",
                    required = false)
            String keyword,

            @RequestParam(
                    value = "withdrawn",
                    required = false)
            Integer withdrawn,

            @RequestParam(
                    value = "roleId",
                    required = false)
            Integer roleId,

            PageRequestDTO pageRequestDTO,

            Model model) {


        /*
         * 잘못된 페이지 방지
         */
        if (pageRequestDTO.getPage() < 1) {

            pageRequestDTO.setPage(1);
        }


        if (pageRequestDTO.getSize() <= 0) {

            pageRequestDTO.setSize(10);
        }


        /*
         * 검색 조건에 맞는 전체 회원 수
         */
        int totalCount =
                adminService.getMemberCount(
                        keyword,
                        withdrawn,
                        roleId
                );


        /*
         * 페이지 응답 객체 생성
         */
        PageResponseDTO pageResponseDTO =
                new PageResponseDTO(
                        pageRequestDTO.getPage(),
                        pageRequestDTO.getSize(),
                        totalCount
                );


        /*
         * 존재하지 않는 큰 페이지 요청 방지
         *
         * 예)
         * 전체가 3페이지인데
         * page=100 요청한 경우
         */
        if (pageResponseDTO.getTotalPages() > 0
                && pageRequestDTO.getPage()
                > pageResponseDTO.getTotalPages()) {

            pageRequestDTO.setPage(
                    pageResponseDTO.getTotalPages()
            );


            /*
             * 변경된 page 값으로
             * ResponseDTO 다시 생성
             */
            pageResponseDTO =
                    new PageResponseDTO(
                            pageRequestDTO.getPage(),
                            pageRequestDTO.getSize(),
                            totalCount
                    );
        }


        /*
         * 현재 페이지 회원 조회
         */
        List<MemberVO> memberList =
                adminService.getMembers(
                        keyword,
                        withdrawn,
                        roleId,
                        pageRequestDTO
                );


        /*
         * 회원 데이터
         */
        model.addAttribute(
                "memberList",
                memberList
        );


        /*
         * 검색 / 필터 정보
         */
        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "withdrawn",
                withdrawn
        );

        model.addAttribute(
                "roleId",
                roleId
        );


        /*
         * PageRequestDTO
         */
        model.addAttribute(
                "pageRequestDTO",
                pageRequestDTO
        );


        /*
         * PageResponseDTO
         */
        model.addAttribute(
                "pageResponseDTO",
                pageResponseDTO
        );


        /*
         * 기존 HTML과 바로 호환되도록
         * 기존 이름도 계속 전달
         *
         * 그래서 member/list.html은
         * 지금 수정하지 않아도 됨
         */
        model.addAttribute(
                "page",
                pageResponseDTO.getPage()
        );

        model.addAttribute(
                "totalPages",
                pageResponseDTO.getTotalPages()
        );

        model.addAttribute(
                "totalCount",
                pageResponseDTO.getTotalCount()
        );


        return "admin/member/list";
    }


    /*
     * =========================================================
     * 회원 권한 변경
     * =========================================================
     */
    @PostMapping("/admin/member/role")
    public String changeMemberRole(

            @RequestParam("memberId")
            Long memberId,

            @RequestParam("roleId")
            Integer roleId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            RedirectAttributes redirectAttributes) {


        try {

            adminService.changeMemberRole(
                    userDetails.getMemberId(),
                    memberId,
                    roleId
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "회원 권한을 변경했습니다."
            );


        } catch (IllegalArgumentException
                | IllegalStateException e) {


            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/member/list";
    }


    /*
     * =========================================================
     * 관리자 회원 탈퇴 처리
     * =========================================================
     */
    @PostMapping("/admin/member/withdraw")
    public String withdrawMember(

            @RequestParam("memberId")
            Long memberId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            RedirectAttributes redirectAttributes) {


        /*
         * 관리자가 자기 자신을
         * 탈퇴시키는 것 방지
         */
        if (userDetails
                .getMemberId()
                .equals(memberId)) {


            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "관리자는 자신의 계정을 관리자 화면에서 탈퇴시킬 수 없습니다."
            );


            return "redirect:/admin/member/list";
        }


        try {

            memberService.withdraw(
                    memberId
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "회원 탈퇴 처리가 완료되었습니다."
            );


        } catch (IllegalArgumentException
                | IllegalStateException e) {


            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/member/list";
    }


    /*
     * =========================================================
     * 관리자 클래스 목록
     * 검색 + 필터 + 페이징
     * =========================================================
     */
    @GetMapping("/admin/class/list")
    public String classList(

            @RequestParam(
                    value = "keyword",
                    required = false)
            String keyword,

            @RequestParam(
                    value = "status",
                    required = false)
            String status,

            PageRequestDTO pageRequestDTO,

            Model model) {


        /*
         * 잘못된 페이지 방지
         */
        if (pageRequestDTO.getPage() < 1) {

            pageRequestDTO.setPage(1);
        }


        if (pageRequestDTO.getSize() <= 0) {

            pageRequestDTO.setSize(10);
        }


        /*
         * 검색 조건에 맞는
         * 전체 클래스 개수
         */
        int totalCount =
                adminService.getClassCount(
                        keyword,
                        status
                );


        /*
         * 페이지 응답 객체 생성
         */
        PageResponseDTO pageResponseDTO =
                new PageResponseDTO(
                        pageRequestDTO.getPage(),
                        pageRequestDTO.getSize(),
                        totalCount
                );


        /*
         * 존재하지 않는 큰 페이지 방지
         */
        if (pageResponseDTO.getTotalPages() > 0
                && pageRequestDTO.getPage()
                > pageResponseDTO.getTotalPages()) {


            pageRequestDTO.setPage(
                    pageResponseDTO.getTotalPages()
            );


            pageResponseDTO =
                    new PageResponseDTO(
                            pageRequestDTO.getPage(),
                            pageRequestDTO.getSize(),
                            totalCount
                    );
        }


        /*
         * 현재 페이지 클래스 조회
         */
        List<ClassVO> classList =
                adminService.getClasses(
                        keyword,
                        status,
                        pageRequestDTO
                );


        /*
         * 클래스 목록
         */
        model.addAttribute(
                "classList",
                classList
        );


        /*
         * 검색 / 필터 정보
         */
        model.addAttribute(
                "keyword",
                keyword
        );

        model.addAttribute(
                "status",
                status
        );


        /*
         * 페이지 DTO
         */
        model.addAttribute(
                "pageRequestDTO",
                pageRequestDTO
        );

        model.addAttribute(
                "pageResponseDTO",
                pageResponseDTO
        );


        /*
         * 기존 class/list.html과
         * 바로 호환되도록 유지
         */
        model.addAttribute(
                "page",
                pageResponseDTO.getPage()
        );

        model.addAttribute(
                "totalPages",
                pageResponseDTO.getTotalPages()
        );

        model.addAttribute(
                "totalCount",
                pageResponseDTO.getTotalCount()
        );


        return "admin/class/list";
    }


    /*
     * =========================================================
     * 클래스 상태 변경
     * =========================================================
     */
    @PostMapping("/admin/class/status")
    public String changeClassStatus(

            @RequestParam("classId")
            Long classId,

            @RequestParam("status")
            String status,

            RedirectAttributes redirectAttributes) {


        try {

            adminService.changeClassStatus(
                    classId,
                    status
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "클래스 상태를 변경했습니다."
            );


        } catch (IllegalArgumentException
                | IllegalStateException e) {


            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/class/list";
    }


    /*
     * =========================================================
     * 관리자 클래스 강제 삭제
     * =========================================================
     */
    @PostMapping("/admin/class/delete")
    public String deleteClass(

            @RequestParam("classId")
            Long classId,

            RedirectAttributes redirectAttributes) {


        try {

            adminService.deleteClass(
                    classId
            );


            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "클래스를 삭제했습니다."
            );


        } catch (IllegalArgumentException
                | IllegalStateException e) {


            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }


        return "redirect:/admin/class/list";
    }
}