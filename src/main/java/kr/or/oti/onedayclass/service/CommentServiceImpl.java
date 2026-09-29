package kr.or.oti.onedayclass.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.CommentDTO;
import kr.or.oti.onedayclass.exception.CommentException;
import kr.or.oti.onedayclass.mapper.CommentMapper;
import kr.or.oti.onedayclass.vo.CommentVO;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;

    public CommentServiceImpl(
            CommentMapper commentMapper) {

        this.commentMapper = commentMapper;
    }


    // 댓글 등록
    @Override
    @Transactional
    public Long registerComment(
            Long classId,
            Long memberId,
            CommentDTO commentDTO) {

        CommentVO commentVO =
                new CommentVO();

        commentVO.setClassId(classId);
        commentVO.setMemberId(memberId);
        commentVO.setContent(
                commentDTO.getContent());

        int result =
                commentMapper.insertComment(
                        commentVO);

        if (result != 1) {
            throw new CommentException (
                    "댓글 등록에 실패했습니다.");
        }

        return commentVO.getCommentId();
    }


    // 특정 클래스 댓글 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<CommentVO> getCommentsByClassId(
            Long classId) {

        return commentMapper
                .selectCommentsByClassId(classId);
    }


    // 댓글 수정
    @Override
    @Transactional
    public void updateComment(
            Long commentId,
            Long memberId,
            CommentDTO commentDTO) {

        // 1. 댓글 조회
        CommentVO commentVO =
                commentMapper.selectCommentById(
                        commentId);

        if (commentVO == null) {
            throw new CommentException (
                    "존재하지 않는 댓글입니다.");
        }

        // 2. 본인이 작성한 댓글인지 확인
        if (!commentVO.getMemberId()
                .equals(memberId)) {

            throw new CommentException(
                    "본인이 작성한 댓글만 수정할 수 있습니다.");
        }

        // 3. 수정할 내용 반영
        commentVO.setContent(
                commentDTO.getContent());

        // 4. 댓글 수정
        int result =
                commentMapper.updateComment(
                        commentVO);

        if (result != 1) {
            throw new CommentException(
                    "댓글 수정에 실패했습니다.");
        }
    }


    // 댓글 삭제
    @Override
    @Transactional
    public void deleteComment(
            Long commentId,
            Long memberId) {

        // 1. 댓글 조회
        CommentVO commentVO =
                commentMapper.selectCommentById(
                        commentId);

        if (commentVO == null) {
            throw new CommentException(
                    "존재하지 않는 댓글입니다.");
        }

        // 2. 본인이 작성한 댓글인지 확인
        if (!commentVO.getMemberId()
                .equals(memberId)) {

            throw new CommentException(
                    "본인이 작성한 댓글만 삭제할 수 있습니다.");
        }

        // 3. 댓글 삭제
        int result =
                commentMapper.deleteComment(
                        commentId,
                        memberId);

        if (result != 1) {
            throw new CommentException(
                    "댓글 삭제에 실패했습니다.");
        }
    }
}