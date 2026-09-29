package kr.or.oti.onedayclass.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import kr.or.oti.onedayclass.dto.ClassRegisterDTO;
import kr.or.oti.onedayclass.dto.ClassSearchDTO;
import kr.or.oti.onedayclass.dto.ClassUpdateDTO;
import kr.or.oti.onedayclass.dto.CommentDTO;
import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.ClassApplicationService;
import kr.or.oti.onedayclass.service.ClassImageService;
import kr.or.oti.onedayclass.service.ClassService;
import kr.or.oti.onedayclass.service.CommentService;
import kr.or.oti.onedayclass.service.ReviewService;
import kr.or.oti.onedayclass.vo.ClassApplicationVO;
import kr.or.oti.onedayclass.vo.ClassImageVO;
import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.CommentVO;
import kr.or.oti.onedayclass.vo.ReviewVO;

@Controller
public class ClassController {

    private final ClassService classService;
    private final ClassApplicationService classApplicationService;
    private final ReviewService reviewService;
    private final CommentService commentService;
    private final ClassImageService classImageService;


    public ClassController(
            ClassService classService,
            ClassApplicationService classApplicationService,
            ReviewService reviewService,
            CommentService commentService,
            ClassImageService classImageService) {

        this.classService = classService;
        this.classApplicationService = classApplicationService;
        this.reviewService = reviewService;
        this.commentService = commentService;
        this.classImageService = classImageService;
    }


    /*
     * =========================================================
     * 클래스 등록 화면
     * =========================================================
     */
    @GetMapping("/class/register")
    public String registerForm(
            Model model) {

        model.addAttribute(
                "classRegisterDTO",
                new ClassRegisterDTO());

        return "class/register";
    }


    /*
     * =========================================================
     * 클래스 등록
     * =========================================================
     *
     * 클래스 DB 저장
     * ↓
     * 생성된 classId 반환
     * ↓
     * 이미지 S3 업로드
     * ↓
     * CLASS_IMAGE 저장
     */
    @PostMapping("/class/register")
    public String register(
            @Valid
            @ModelAttribute("classRegisterDTO")
            ClassRegisterDTO registerDTO,

            BindingResult bindingResult,

            @RequestParam(
                    value = "images",
                    required = false)
            List<MultipartFile> images,

            @AuthenticationPrincipal
            CustomUserDetails userDetails) {


        if (bindingResult.hasErrors()) {

            return "class/register";
        }


        /*
         * 클래스 정보 먼저 등록
         */
        Long classId =
                classService.register(
                        registerDTO,
                        userDetails.getMemberId());


        /*
         * 이미지 S3 업로드
         */
        classImageService.uploadClassImages(
                classId,
                images);


        return "redirect:/class/detail?classId="
                + classId;
    }


    /*
     * =========================================================
     * 클래스 목록
     * =========================================================
     *
     * 클래스 목록과 함께 각 클래스의
     * 대표 이미지 Presigned URL을 화면으로 전달
     */
    @GetMapping("/class/list")
    public String classList(
            @ModelAttribute("searchDTO")
            ClassSearchDTO searchDTO,

            Model model) {


        /*
         * 검색 조건에 맞는 클래스 목록 조회
         */
        List<ClassVO> classList =
                classService.getClassList(
                        searchDTO);


        /*
         * 클래스별 대표 이미지 URL 저장
         *
         * key:
         * classId
         *
         * value:
         * 대표 이미지 Presigned URL
         */
        Map<Long, String> thumbnailMap =
                new HashMap<>();


        /*
         * 클래스마다 대표 이미지 조회
         */
        for (ClassVO classVO : classList) {


            ClassImageVO thumbnail =
                    classImageService
                        .getThumbnail(
                                classVO.getClassId());


            /*
             * 대표 이미지가 있는 클래스만
             * Map에 추가
             */
            if (thumbnail != null) {

                thumbnailMap.put(
                        classVO.getClassId(),
                        thumbnail.getPresignedUrl());
            }
        }


        /*
         * 클래스 목록
         */
        model.addAttribute(
                "classList",
                classList);


        /*
         * 클래스별 대표 이미지
         */
        model.addAttribute(
                "thumbnailMap",
                thumbnailMap);


        return "class/list";
    }


    /*
     * =========================================================
     * 클래스 상세
     * =========================================================
     */
    @GetMapping("/class/detail")
    public String classDetail(
            @RequestParam("classId")
            Long classId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            Model model) {


        /*
         * 클래스 기본 정보
         */
        ClassVO classVO =
                classService.getClass(
                        classId);


        /*
         * 현재 참여 인원
         */
        int currentParticipantCount =
                classApplicationService
                        .getCurrentParticipantCount(
                                classId);


        /*
         * 리뷰
         */
        List<ReviewVO> reviewList =
                reviewService
                        .getReviewsByClassId(
                                classId);


        /*
         * 댓글
         */
        List<CommentVO> commentList =
                commentService
                        .getCommentsByClassId(
                                classId);


        /*
         * S3 이미지
         *
         * ClassImageService에서
         * Presigned URL까지 생성해서 반환
         */
        List<ClassImageVO> classImages =
                classImageService
                        .getImagesByClassId(
                                classId);


        boolean isHost =
                false;

        boolean hasApplication =
                false;

        boolean canWriteReview =
                false;


        ClassApplicationVO myApplication =
                null;

        ReviewVO myReview =
                null;


        /*
         * 로그인 사용자 정보 처리
         */
        if (userDetails != null) {

            Long loginMemberId =
                    userDetails.getMemberId();


            isHost =
                    classVO.getHostId()
                            .equals(
                                    loginMemberId);


            /*
             * 개설자가 아닌 사용자
             */
            if (!isHost) {

                myApplication =
                        classApplicationService
                                .getApplication(
                                        classId,
                                        loginMemberId);


                hasApplication =
                        myApplication != null;


                myReview =
                        reviewService
                                .getMyReview(
                                        classId,
                                        loginMemberId);


                canWriteReview =
                        myApplication != null
                        && "COMPLETED".equals(
                                myApplication.getStatus())
                        && myReview == null;
            }
        }


        /*
         * 클래스
         */
        model.addAttribute(
                "classVO",
                classVO);


        model.addAttribute(
                "currentParticipantCount",
                currentParticipantCount);


        model.addAttribute(
                "isHost",
                isHost);


        /*
         * 이미지
         */
        model.addAttribute(
                "classImages",
                classImages);


        /*
         * 신청
         */
        model.addAttribute(
                "hasApplication",
                hasApplication);


        model.addAttribute(
                "myApplication",
                myApplication);


        /*
         * 리뷰
         */
        model.addAttribute(
                "reviewList",
                reviewList);


        model.addAttribute(
                "myReview",
                myReview);


        model.addAttribute(
                "canWriteReview",
                canWriteReview);


        /*
         * 댓글
         */
        model.addAttribute(
                "commentList",
                commentList);


        model.addAttribute(
                "commentDTO",
                new CommentDTO());


        return "class/detail";
    }


    /*
     * =========================================================
     * 클래스 수정 화면
     * =========================================================
     */
    @GetMapping("/class/update")
    public String updateForm(
            @RequestParam("classId")
            Long classId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            Model model) {


        ClassVO classVO =
                classService.getClass(
                        classId);


        /*
         * 개설자만 수정 가능
         */
        if (!classVO.getHostId()
                .equals(
                        userDetails.getMemberId())) {

            throw new SecurityException(
                    "클래스를 수정할 권한이 없습니다.");
        }


        /*
         * 기존 클래스 정보를
         * 수정 DTO로 옮긴다.
         */
        ClassUpdateDTO updateDTO =
                new ClassUpdateDTO();


        updateDTO.setClassId(
                classVO.getClassId());


        updateDTO.setCategoryId(
                classVO.getCategoryId());


        updateDTO.setTitle(
                classVO.getTitle());


        updateDTO.setDescription(
                classVO.getDescription());


        updateDTO.setPrice(
                classVO.getPrice());


        updateDTO.setCapacity(
                classVO.getCapacity());


        updateDTO.setClassDate(
                classVO.getClassDate());


        updateDTO.setDeadline(
                classVO.getDeadline());


        updateDTO.setLocation(
                classVO.getLocation());


        /*
         * 수정 화면에서 보여줄
         * 기존 이미지 목록
         */
        List<ClassImageVO> classImages =
                classImageService
                        .getImagesByClassId(
                                classId);


        model.addAttribute(
                "classUpdateDTO",
                updateDTO);


        model.addAttribute(
                "classImages",
                classImages);


        return "class/update";
    }


    /*
     * =========================================================
     * 클래스 수정 처리
     * =========================================================
     *
     * 1. 클래스 기본 정보 수정
     * 2. 선택한 기존 이미지 삭제
     * 3. 대표 이미지 변경
     * 4. 새 이미지 추가
     */
    @PostMapping("/class/update")
    public String update(
            @Valid
            @ModelAttribute("classUpdateDTO")
            ClassUpdateDTO updateDTO,

            BindingResult bindingResult,

            /*
             * 새로 추가할 이미지
             */
            @RequestParam(
                    value = "newImages",
                    required = false)
            List<MultipartFile> newImages,

            /*
             * 사용자가 삭제 체크한
             * 기존 이미지 ID
             */
            @RequestParam(
                    value = "deleteImageIds",
                    required = false)
            List<Long> deleteImageIds,

            /*
             * 사용자가 선택한
             * 대표 이미지 ID
             */
            @RequestParam(
                    value = "thumbnailImageId",
                    required = false)
            Long thumbnailImageId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            Model model) {


        /*
         * DTO 검증 실패
         */
        if (bindingResult.hasErrors()) {


            /*
             * 검증 오류로 수정 화면으로 다시 갈 때도
             * 기존 이미지가 보여야 한다.
             */
            List<ClassImageVO> classImages =
                    classImageService
                            .getImagesByClassId(
                                    updateDTO.getClassId());


            model.addAttribute(
                    "classImages",
                    classImages);


            return "class/update";
        }


        /*
         * 클래스 정보 수정
         *
         * ClassService에서
         * 개설자 권한도 검사
         */
        classService.updateClass(
                updateDTO,
                userDetails.getMemberId());


        /*
         * 기존 이미지 중
         * 삭제 체크한 이미지 삭제
         *
         * S3 + DB 둘 다 삭제
         */
        classImageService.deleteImages(
                updateDTO.getClassId(),
                deleteImageIds);


        /*
         * 대표 이미지 변경
         */
        classImageService.changeThumbnail(
                updateDTO.getClassId(),
                thumbnailImageId);


        /*
         * 새 이미지 추가
         *
         * S3 업로드
         * +
         * CLASS_IMAGE INSERT
         */
        classImageService.addClassImages(
                updateDTO.getClassId(),
                newImages);


        return "redirect:/class/detail?classId="
                + updateDTO.getClassId();
    }


    /*
     * =========================================================
     * 클래스 삭제
     * =========================================================
     */
    @PostMapping("/class/delete")
    public String delete(
            @RequestParam("classId")
            Long classId,

            @AuthenticationPrincipal
            CustomUserDetails userDetails) {


        /*
         * 먼저 클래스 정보를 조회해서
         * 현재 로그인 사용자가 개설자인지 검사
         *
         * S3 파일을 먼저 지우기 때문에
         * 권한 검사를 여기서 먼저 하는 것이 중요하다.
         */
        ClassVO classVO =
                classService.getClass(
                        classId);


        if (!classVO.getHostId()
                .equals(
                        userDetails.getMemberId())) {

            throw new SecurityException(
                    "클래스를 삭제할 권한이 없습니다.");
        }


        /*
         * S3 이미지 먼저 삭제
         *
         * 클래스 DB를 먼저 삭제하면
         * ON DELETE CASCADE 때문에
         * CLASS_IMAGE 데이터도 사라져
         *
         * S3 object key를 알 수 없게 된다.
         */
        classImageService
                .deleteAllImagesByClassId(
                        classId);


        /*
         * 클래스 DB 삭제
         */
        classService.deleteClass(
                classId,
                userDetails.getMemberId());


        return "redirect:/class/list";
    }
}