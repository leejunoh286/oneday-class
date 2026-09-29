package kr.or.oti.onedayclass.service;

import java.util.List;

import kr.or.oti.onedayclass.dto.CommentDTO;
import kr.or.oti.onedayclass.vo.CommentVO;

public interface CommentService {

    // 댓글 등록
    Long registerComment(
            Long classId,
            Long memberId,
            CommentDTO commentDTO);

    // 특정 클래스의 댓글 목록 조회
    List<CommentVO> getCommentsByClassId(
            Long classId);

    // 댓글 수정
    void updateComment(
            Long commentId,
            Long memberId,
            CommentDTO commentDTO);

    // 댓글 삭제
    void deleteComment(
            Long commentId,
            Long memberId);
}