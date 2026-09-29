package kr.or.oti.onedayclass.controller;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import kr.or.oti.onedayclass.dto.CommentDTO;
import kr.or.oti.onedayclass.mapper.CommentMapper;
import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.CommentService;
import kr.or.oti.onedayclass.vo.CommentVO;

@Controller
public class CommentController {

    private final CommentService commentService;
    private final CommentMapper commentMapper;

    public CommentController(
            CommentService commentService,
            CommentMapper commentMapper) {

        this.commentService = commentService;
        this.commentMapper = commentMapper;
    }


    // 댓글 등록
    @PostMapping("/comment/register")
    public String register(
            @RequestParam("classId") Long classId,
            @Valid CommentDTO commentDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "redirect:/class/detail?classId=" + classId;
        }

        commentService.registerComment(
                classId,
                userDetails.getMemberId(),
                commentDTO);

        return "redirect:/class/detail?classId=" + classId;
    }


    // 댓글 수정 폼
    @GetMapping("/comment/update")
    public String updateForm(
            @RequestParam("commentId") Long commentId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        CommentVO commentVO =
                commentMapper.selectCommentById(
                        commentId);

        if (commentVO == null) {
            return "redirect:/class/detail?classId=" + classId;
        }

        if (!commentVO.getMemberId()
                .equals(userDetails.getMemberId())) {

            return "redirect:/class/detail?classId=" + classId;
        }

        CommentDTO commentDTO =
                new CommentDTO();

        commentDTO.setContent(
                commentVO.getContent());

        model.addAttribute(
                "commentId",
                commentId);

        model.addAttribute(
                "classId",
                classId);

        model.addAttribute(
                "commentDTO",
                commentDTO);

        return "comment/update";
    }


    // 댓글 수정
    @PostMapping("/comment/update")
    public String update(
            @RequestParam("commentId") Long commentId,
            @RequestParam("classId") Long classId,
            @Valid CommentDTO commentDTO,
            BindingResult bindingResult,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "commentId",
                    commentId);

            model.addAttribute(
                    "classId",
                    classId);

            return "comment/update";
        }

        commentService.updateComment(
                commentId,
                userDetails.getMemberId(),
                commentDTO);

        return "redirect:/class/detail?classId=" + classId;
    }


    // 댓글 삭제
    @PostMapping("/comment/delete")
    public String delete(
            @RequestParam("commentId") Long commentId,
            @RequestParam("classId") Long classId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        commentService.deleteComment(
                commentId,
                userDetails.getMemberId());

        return "redirect:/class/detail?classId=" + classId;
    }
}