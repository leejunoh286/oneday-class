package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.vo.CommentVO;

@Mapper
public interface CommentMapper {

    // 댓글 등록
    int insertComment(CommentVO commentVO);

    // 특정 클래스의 댓글 목록 조회
    List<CommentVO> selectCommentsByClassId(Long classId);

    // 댓글 번호로 댓글 조회
    CommentVO selectCommentById(Long commentId);

    // 댓글 수정
    int updateComment(CommentVO commentVO);

    // 댓글 삭제
    int deleteComment(
            @Param("commentId") Long commentId,
            @Param("memberId") Long memberId);
}