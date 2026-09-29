package kr.or.oti.onedayclass.controller;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.oti.onedayclass.dto.ReviewRegisterDTO;
import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.ReviewService;
import kr.or.oti.onedayclass.vo.ReviewVO;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(
            ReviewService reviewService) {

        this.reviewService = reviewService;
    }


    // 리뷰 작성 폼
    @GetMapping("/review/register")
    public String registerForm(
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        // 이미 작성한 리뷰가 있는지 확인
        ReviewVO myReview =
                reviewService.getMyReview(
                        classId,
                        userDetails.getMemberId());

        if (myReview != null) {
            return "redirect:/class/detail?classId=" + classId;
        }

        model.addAttribute(
                "classId",
                classId);

        model.addAttribute(
                "reviewRegisterDTO",
                new ReviewRegisterDTO());

        return "review/register";
    }


    // 리뷰 등록
    @PostMapping("/review/register")
    public String register(
            @RequestParam("classId") Long classId,
            @Valid ReviewRegisterDTO reviewRegisterDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "classId",
                    classId);

            return "review/register";
        }

        reviewService.registerReview(
                classId,
                userDetails.getMemberId(),
                reviewRegisterDTO);

        return "redirect:/class/detail?classId=" + classId;
    }


    // 리뷰 수정 폼
    @GetMapping("/review/update")
    public String updateForm(
            @RequestParam("reviewId") Long reviewId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        ReviewVO myReview =
                reviewService.getMyReview(
                        classId,
                        userDetails.getMemberId());

        if (myReview == null
                || !myReview.getReviewId().equals(reviewId)) {

            return "redirect:/class/detail?classId=" + classId;
        }

        ReviewRegisterDTO reviewRegisterDTO =
                new ReviewRegisterDTO();

        reviewRegisterDTO.setRating(
                myReview.getRating());

        reviewRegisterDTO.setContent(
                myReview.getContent());

        model.addAttribute(
                "reviewId",
                reviewId);

        model.addAttribute(
                "classId",
                classId);

        model.addAttribute(
                "reviewRegisterDTO",
                reviewRegisterDTO);

        return "review/update";
    }


    // 리뷰 수정
    @PostMapping("/review/update")
    public String update(
            @RequestParam("reviewId") Long reviewId,
            @RequestParam("classId") Long classId,
            @Valid ReviewRegisterDTO reviewRegisterDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "reviewId",
                    reviewId);

            model.addAttribute(
                    "classId",
                    classId);

            return "review/update";
        }

        reviewService.updateReview(
                reviewId,
                userDetails.getMemberId(),
                reviewRegisterDTO);

        return "redirect:/class/detail?classId=" + classId;
    }


    // 리뷰 삭제
    @PostMapping("/review/delete")
    public String delete(
            @RequestParam("reviewId") Long reviewId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        reviewService.deleteReview(
                reviewId,
                userDetails.getMemberId());

        return "redirect:/class/detail?classId=" + classId;
    }
}