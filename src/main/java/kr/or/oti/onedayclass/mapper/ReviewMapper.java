package kr.or.oti.onedayclass.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.oti.onedayclass.vo.ReviewVO;

@Mapper
public interface ReviewMapper {

    // 리뷰 등록
    int insertReview(ReviewVO reviewVO);

    // 특정 클래스의 리뷰 목록 조회
    List<ReviewVO> selectReviewsByClassId(Long classId);

    // 특정 회원이 특정 클래스에 작성한 리뷰 조회
    ReviewVO selectReviewByMemberAndClass(
            @Param("memberId") Long memberId,
            @Param("classId") Long classId);

    // 리뷰 번호로 리뷰 조회
    ReviewVO selectReviewById(Long reviewId);

    // 리뷰 수정
    int updateReview(ReviewVO reviewVO);

    // 리뷰 삭제
    int deleteReview(
            @Param("reviewId") Long reviewId,
            @Param("memberId") Long memberId);
}