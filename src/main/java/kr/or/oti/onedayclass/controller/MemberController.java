package kr.or.oti.onedayclass.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import kr.or.oti.onedayclass.dto.MemberJoinDTO;
import kr.or.oti.onedayclass.dto.MemberUpdateDTO;
import kr.or.oti.onedayclass.dto.MyPageApplicationDTO;
import kr.or.oti.onedayclass.dto.PasswordChangeDTO;
import kr.or.oti.onedayclass.exception.DuplicateMemberException;
import kr.or.oti.onedayclass.security.CustomUserDetails;
import kr.or.oti.onedayclass.service.ClassApplicationService;
import kr.or.oti.onedayclass.service.ClassService;
import kr.or.oti.onedayclass.service.MemberService;
import kr.or.oti.onedayclass.vo.ClassVO;
import kr.or.oti.onedayclass.vo.MemberVO;

@Controller
public class MemberController {

    private final MemberService memberService;
    private final ClassService classService;
    private final ClassApplicationService classApplicationService;

    public MemberController(
            MemberService memberService,
            ClassService classService,
            ClassApplicationService classApplicationService) {

        this.memberService = memberService;
        this.classService = classService;
        this.classApplicationService = classApplicationService;
    }


    // 회원가입 화면
    @GetMapping("/member/join")
    public String joinForm(Model model) {

        model.addAttribute(
                "memberJoinDTO",
                new MemberJoinDTO()
        );

        return "member/join";
    }


    // 회원가입 처리
    @PostMapping("/member/join")
    public String join(
            @Valid
            @ModelAttribute("memberJoinDTO")
            MemberJoinDTO joinDTO,
            BindingResult bindingResult) {

        // Bean Validation 실패
        if (bindingResult.hasErrors()) {
            return "member/join";
        }

        try {
            memberService.join(joinDTO);

        } catch (DuplicateMemberException e) {

            // 중복된 필드에 직접 오류 추가
            bindingResult.rejectValue(
                    e.getField(),
                    "duplicate",
                    e.getMessage()
            );

            return "member/join";
        }

        return "redirect:/member/login";
    }


    // 로그인 화면
    @GetMapping("/member/login")
    public String loginForm() {

        return "member/login";
    }
    
    // 회원정보 수정 화면(수정 화면을 처음 열었을 때 기존 회원 정보를 가져와서 input에 미리 넣어준다)
    @GetMapping("/member/edit")
    public String editForm(
    		@AuthenticationPrincipal
    		CustomUserDetails userDetails,
    		Model model) {
    	
    	Long memberId = userDetails.getMemberId();
    	
    	MemberVO member = memberService.getMemberById(memberId);
    	
    	MemberUpdateDTO updateDTO = new MemberUpdateDTO();
    	
    	updateDTO.setName(member.getName());
    	
    	updateDTO.setNickname(member.getNickname());
    	
    	updateDTO.setEmail(member.getEmail());
    	
    	updateDTO.setPhone(member.getPhone());
    	
    	model.addAttribute("memberUpdateDTO", updateDTO);
    	
    	return "member/edit";
    }
    
    
    /*
     * =========================================================
     * 회원정보 수정 처리
     * =========================================================
     */
    @PostMapping("/member/edit")
    public String edit(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,

            @Valid
            @ModelAttribute("memberUpdateDTO")
            MemberUpdateDTO updateDTO,

            BindingResult bindingResult) {


        /* Bean Validation 실패 */
        if (bindingResult.hasErrors()) {

            return "member/edit";
        }


        try {

            memberService.updateMember(
                    userDetails.getMemberId(),
                    updateDTO
            );


        } catch (DuplicateMemberException e) {

            bindingResult.rejectValue(
                    e.getField(),
                    "duplicate",
                    e.getMessage()
            );

            return "member/edit";
        }


        return "redirect:/member/mypage";
    }
    
    // =========================================
    // 비밀번호 변경 화면
    // =========================================
    @GetMapping("/member/password")
    public String passwordForm(Model model) {
    	model.addAttribute("passwordChangeDTO", new PasswordChangeDTO());
    	
    	return "member/password";
    }
    
    // =========================================
    // 비밀번호 변경 처리
    // =========================================
    @PostMapping("/member/password")
    public String changePassword(
    		@AuthenticationPrincipal
    		CustomUserDetails userDetails,
    		@Valid
    		@ModelAttribute("passwordChangeDTO")
    		PasswordChangeDTO passwordChangeDTO,
    		BindingResult bindingResult,
    		Model model) {
    	
    	/* Bean Validation 실패 */
    	if(bindingResult.hasErrors()) {
    		return "member/password";
    	}
    	
    	try {
    		memberService.changePassword(
    				userDetails.getMemberId(), 
    				passwordChangeDTO
    		);
    		
    	} catch(IllegalArgumentException e) {
    		model.addAttribute("passwordError", e.getMessage());
    		
    		return "member/password";
    	}
    	
    	return "redirect:/member/mypage";
    }


    // =========================================
    // 마이페이지
    // =========================================

    @GetMapping("/member/mypage")
    public String myPage(
            @AuthenticationPrincipal
            CustomUserDetails userDetails,
            Model model) {

        Long memberId =
                userDetails.getMemberId();


        // 내가 개설한 클래스
        List<ClassVO> openedClassList =
                classService.getClassesByHostId(
                        memberId);


        // 내가 신청한 클래스
        List<MyPageApplicationDTO> appliedClassList =
                classApplicationService
                        .getApplicationsByMemberId(
                                memberId);


        model.addAttribute(
                "openedClassList",
                openedClassList);

        model.addAttribute(
                "appliedClassList",
                appliedClassList);


        // 로그인 회원 기본 정보
        model.addAttribute(
                "loginMemberId",
                memberId);

        model.addAttribute(
                "nickname",
                userDetails.getNickname());


        return "member/mypage";
    }
    
    
    @PostMapping("/member/withdraw")
    public String withdraw(
    		@AuthenticationPrincipal
    		CustomUserDetails userDetails,
    		HttpServletRequest request,
    		RedirectAttributes redirectAttributes) {
    	
    	Long memberId = userDetails.getMemberId();
    	
    	try {
    		memberService.withdraw(memberId);
    	
    		request.getSession().invalidate();
    	
    		return "redirect:/";
    	
    	} catch(IllegalStateException e) {
    		redirectAttributes.addFlashAttribute(
    				"withdrawError",
    				e.getMessage()
    		);
    		
    		return "redirect:/member/mypage";
    	}
    }
    
}











