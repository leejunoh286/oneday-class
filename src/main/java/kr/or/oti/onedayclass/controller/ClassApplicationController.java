package kr.or.oti.onedayclass.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.ClassApplicationService;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;

@Controller
public class ClassApplicationController {

    private final ClassApplicationService classApplicationService;

    public ClassApplicationController(
            ClassApplicationService classApplicationService) {

        this.classApplicationService = classApplicationService;
    }


    // 클래스 신청
    @PostMapping("/class/application/apply")
    public String apply(
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        classApplicationService.apply(
                classId,
                userDetails.getMemberId());

        return "redirect:/class/detail?classId=" + classId;
    }


    // 클래스 신청 취소
    @PostMapping("/class/application/cancel")
    public String cancel(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        classApplicationService.cancel(
                applicationId,
                userDetails.getMemberId());

        return "redirect:/class/detail?classId=" + classId;
    }


    // =========================================
    // 개설자 - 신청자 목록
    // =========================================

    @GetMapping("/class/application/manage")
    public String manage(
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        List<ClassApplicationVO> applicationList =
                classApplicationService.getApplicationsByClass(
                        classId,
                        userDetails.getMemberId());

        model.addAttribute("classId", classId);
        model.addAttribute(
                "applicationList",
                applicationList);

        return "class/application-manage";
    }


    // =========================================
    // 개설자 - 신청 승인
    // =========================================

    @PostMapping("/class/application/approve")
    public String approve(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        classApplicationService.approve(
                applicationId,
                userDetails.getMemberId());

        return "redirect:/class/application/manage?classId="
                + classId;
    }


    // =========================================
    // 개설자 - 신청 거절
    // =========================================

    @PostMapping("/class/application/reject")
    public String reject(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        classApplicationService.reject(
                applicationId,
                userDetails.getMemberId());

        return "redirect:/class/application/manage?classId="
                + classId;
    }


    // =========================================
    // 개설자 - 수강 완료 처리
    // =========================================

    @PostMapping("/class/application/complete")
    public String complete(
            @RequestParam("applicationId") Long applicationId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        classApplicationService.complete(
                applicationId,
                userDetails.getMemberId());

        return "redirect:/class/application/manage?classId="
                + classId;
    }
}