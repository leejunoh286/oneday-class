package kr.or.oti.onedayclass.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.ReviewRegisterDTO;
import kr.or.oti.onedayclass.exception.ReviewException;
import kr.or.oti.onedayclass.mapper.ClassApplicationMapper;
import kr.or.oti.onedayclass.mapper.ReviewMapper;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;
import kr.or.oti.onedayclass.vo.ReviewVO;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final ClassApplicationMapper classApplicationMapper;

    public ReviewServiceImpl(
            ReviewMapper reviewMapper,
            ClassApplicationMapper classApplicationMapper) {

        this.reviewMapper = reviewMapper;
        this.classApplicationMapper = classApplicationMapper;
    }


    // 리뷰 등록
    @Override
    @Transactional
    public Long registerReview(
            Long classId,
            Long memberId,
            ReviewRegisterDTO reviewRegisterDTO) {

        // 1. 해당 클래스의 신청 내역 조회
        ClassApplicationVO application =
                classApplicationMapper.selectApplication(
                        memberId,
                        classId);

        // 2. 신청 내역 존재 여부 확인
        if (application == null) {
            throw new ReviewException (
                    "수강 신청 내역이 없는 클래스에는 리뷰를 작성할 수 없습니다.");
        }

        // 3. 수강 완료 여부 확인
        if (!"COMPLETED".equals(application.getStatus())) {
            throw new ReviewException(
                    "수강 완료된 클래스에만 리뷰를 작성할 수 있습니다.");
        }

        // 4. 기존 리뷰 존재 여부 확인
        ReviewVO existingReview =
                reviewMapper.selectReviewByMemberAndClass(
                        memberId,
                        classId);

        if (existingReview != null) {
            throw new ReviewException(
                    "이미 리뷰를 작성한 클래스입니다.");
        }

        // 5. 리뷰 객체 생성
        ReviewVO reviewVO = new ReviewVO();

        reviewVO.setMemberId(memberId);
        reviewVO.setClassId(classId);
        reviewVO.setRating(
                reviewRegisterDTO.getRating());
        reviewVO.setContent(
                reviewRegisterDTO.getContent());

        // 6. 리뷰 등록
        int result =
                reviewMapper.insertReview(reviewVO);

        if (result != 1) {
            throw new ReviewException(
                    "리뷰 등록에 실패했습니다.");
        }

        return reviewVO.getReviewId();
    }


    // 특정 클래스 리뷰 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ReviewVO> getReviewsByClassId(
            Long classId) {

        return reviewMapper
                .selectReviewsByClassId(classId);
    }


    // 로그인 회원이 작성한 리뷰 조회
    @Override
    @Transactional(readOnly = true)
    public ReviewVO getMyReview(
            Long classId,
            Long memberId) {

        return reviewMapper
                .selectReviewByMemberAndClass(
                        memberId,
                        classId);
    }


    // 리뷰 수정
    @Override
    @Transactional
    public void updateReview(
            Long reviewId,
            Long memberId,
            ReviewRegisterDTO reviewRegisterDTO) {

        // 1. 리뷰 조회
        ReviewVO reviewVO =
                reviewMapper.selectReviewById(
                        reviewId);

        if (reviewVO == null) {
            throw new ReviewException (
                    "존재하지 않는 리뷰입니다.");
        }

        // 2. 본인이 작성한 리뷰인지 확인
        if (!reviewVO.getMemberId()
                .equals(memberId)) {

            throw new ReviewException (
                    "본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 3. 변경값 반영
        reviewVO.setRating(
                reviewRegisterDTO.getRating());

        reviewVO.setContent(
                reviewRegisterDTO.getContent());

        // 4. 리뷰 수정
        int result =
                reviewMapper.updateReview(reviewVO);

        if (result != 1) {
            throw new ReviewException (
                    "리뷰 수정에 실패했습니다.");
        }
    }


    // 리뷰 삭제
    @Override
    @Transactional
    public void deleteReview(
            Long reviewId,
            Long memberId) {

        // 1. 리뷰 조회
        ReviewVO reviewVO =
                reviewMapper.selectReviewById(
                        reviewId);

        if (reviewVO == null) {
            throw new ReviewException (
                    "존재하지 않는 리뷰입니다.");
        }

        // 2. 본인 리뷰인지 확인
        if (!reviewVO.getMemberId()
                .equals(memberId)) {

            throw new ReviewException (
                    "본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        // 3. 리뷰 삭제
        int result =
                reviewMapper.deleteReview(
                        reviewId,
                        memberId);

        if (result != 1) {
            throw new ReviewException (
                    "리뷰 삭제에 실패했습니다.");
        }
    }
}
