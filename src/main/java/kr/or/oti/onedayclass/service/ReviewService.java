package kr.or.oti.onedayclass.service;

import java.util.List;

import kr.or.oti.onedayclass.dto.ReviewRegisterDTO;
import kr.or.oti.onedayclass.vo.ReviewVO;

public interface ReviewService {

    // 리뷰 등록
    Long registerReview(
            Long classId,
            Long memberId,
            ReviewRegisterDTO reviewRegisterDTO);

    // 특정 클래스 리뷰 목록 조회
    List<ReviewVO> getReviewsByClassId(Long classId);

    // 특정 회원이 특정 클래스에 작성한 리뷰 조회
    ReviewVO getMyReview(
            Long classId,
            Long memberId);

    // 리뷰 수정
    void updateReview(
            Long reviewId,
            Long memberId,
            ReviewRegisterDTO reviewRegisterDTO);

    // 리뷰 삭제
    void deleteReview(
            Long reviewId,
            Long memberId);
}