package kr.or.oti.onedayclass.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.oti.onedayclass.dto.ClassRegisterDTO;
import kr.or.oti.onedayclass.dto.ClassSearchDTO;
import kr.or.oti.onedayclass.dto.ClassUpdateDTO;
import kr.or.oti.onedayclass.mapper.ClassMapper;
import kr.or.oti.onedayclass.vo.ClassVO;

@Service
public class ClassServiceImpl implements ClassService {

    private final ClassMapper classMapper;

    public ClassServiceImpl(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }


    // 클래스 등록
    @Override
    @Transactional
    public Long register(
            ClassRegisterDTO registerDTO,
            Long hostId) {

        // 모집 마감일은 클래스 진행일보다 늦을 수 없음
        if (registerDTO.getDeadline()
                .isAfter(registerDTO.getClassDate())) {

            throw new IllegalArgumentException(
                    "모집 마감일은 클래스 진행일보다 늦을 수 없습니다."
            );
        }

        ClassVO classVO = new ClassVO();

        classVO.setHostId(hostId);
        classVO.setCategoryId(
                registerDTO.getCategoryId());
        classVO.setTitle(
                registerDTO.getTitle());
        classVO.setDescription(
                registerDTO.getDescription());
        classVO.setPrice(
                registerDTO.getPrice());
        classVO.setCapacity(
                registerDTO.getCapacity());
        classVO.setClassDate(
                registerDTO.getClassDate());
        classVO.setDeadline(
                registerDTO.getDeadline());
        classVO.setLocation(
                registerDTO.getLocation());

        // 신규 클래스는 모집 중 상태로 시작
        classVO.setStatus("RECRUITING");

        int result =
                classMapper.insertClass(classVO);

        if (result != 1) {
            throw new IllegalStateException(
                    "클래스 등록 처리 중 오류가 발생했습니다."
            );
        }

        // 날짜 기준으로 상태 다시 계산
        classMapper.updateClassStatus(
                classVO.getClassId());

        return classVO.getClassId();
    }


    // =========================================
    // 기존 클래스 전체 목록 조회
    // =========================================

    @Override
    @Transactional
    public List<ClassVO> getClassList() {

        // 목록 조회 전 상태 최신화
        classMapper.updateAllClassStatuses();

        return classMapper.selectClassList();
    }


    // =========================================
    // 검색 + 페이징 클래스 목록 조회
    // =========================================

    @Override
    @Transactional
    public List<ClassVO> getClassList(
            ClassSearchDTO searchDTO) {

        // 1. DTO가 null이면 기본 검색 조건 생성
        if (searchDTO == null) {
            searchDTO = new ClassSearchDTO();
        }

        // 2. 페이지 번호 보정
        if (searchDTO.getPage() < 1) {
            searchDTO.setPage(1);
        }

        // 3. 페이지 크기 보정
        if (searchDTO.getSize() <= 0) {
            searchDTO.setSize(10);
        }

        // 너무 많은 데이터를 한 번에 요청하지 못하도록 제한
        if (searchDTO.getSize() > 100) {
            searchDTO.setSize(100);
        }

        // 4. 검색어 앞뒤 공백 제거
        if (searchDTO.getKeyword() != null) {

            String keyword =
                    searchDTO.getKeyword().trim();

            if (keyword.isEmpty()) {
                searchDTO.setKeyword(null);
            } else {
                searchDTO.setKeyword(keyword);
            }
        }

        // 5. 모든 클래스 상태 최신화
        classMapper.updateAllClassStatuses();

        // 6. 검색조건에 해당하는 전체 데이터 개수 조회
        int totalCount =
                classMapper.countClassListBySearch(
                        searchDTO);

        searchDTO.setTotalCount(totalCount);

        // 7. 전체 페이지 수 계산
        searchDTO.calculateTotalPages();

        /*
         * 사용자가 실제 전체 페이지보다 큰 페이지를 요청한 경우
         * 마지막 페이지로 보정
         *
         * 단, 검색 결과가 0개이면 page는 1로 유지
         */
        if (searchDTO.getTotalPages() > 0
                && searchDTO.getPage()
                        > searchDTO.getTotalPages()) {

            searchDTO.setPage(
                    searchDTO.getTotalPages());
        }

        // 8. 현재 페이지의 클래스 목록 조회
        return classMapper
                .selectClassListBySearch(
                        searchDTO);
    }


    // =========================================
    // 마이페이지 - 내가 개설한 클래스 목록 조회
    // =========================================

    @Override
    @Transactional
    public List<ClassVO> getClassesByHostId(
            Long hostId) {

        // 마이페이지에 표시하기 전에
        // 클래스 상태를 현재 시간 기준으로 최신화
        classMapper.updateAllClassStatuses();

        return classMapper
                .selectClassesByHostId(hostId);
    }


    // =========================================
    // 클래스 상세 조회
    // =========================================

    @Override
    @Transactional
    public ClassVO getClass(Long classId) {

        // 해당 클래스 상태 최신화
        classMapper.updateClassStatus(classId);

        ClassVO classVO =
                classMapper.selectClassById(classId);

        if (classVO == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다."
            );
        }

        return classVO;
    }


    // =========================================
    // 클래스 수정
    // =========================================

    @Override
    @Transactional
    public void updateClass(
            ClassUpdateDTO updateDTO,
            Long loginMemberId) {

        ClassVO savedClass =
                classMapper.selectClassById(
                        updateDTO.getClassId());

        if (savedClass == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다."
            );
        }

        // 실제 개설자와 로그인 회원 비교
        if (!savedClass.getHostId()
                .equals(loginMemberId)) {

            throw new SecurityException(
                    "클래스를 수정할 권한이 없습니다."
            );
        }

        // 모집 마감일 검사
        if (updateDTO.getDeadline()
                .isAfter(updateDTO.getClassDate())) {

            throw new IllegalArgumentException(
                    "모집 마감일은 클래스 진행일보다 늦을 수 없습니다."
            );
        }

        // 수정 가능한 값만 반영
        savedClass.setCategoryId(
                updateDTO.getCategoryId());

        savedClass.setTitle(
                updateDTO.getTitle());

        savedClass.setDescription(
                updateDTO.getDescription());

        savedClass.setPrice(
                updateDTO.getPrice());

        savedClass.setCapacity(
                updateDTO.getCapacity());

        savedClass.setClassDate(
                updateDTO.getClassDate());

        savedClass.setDeadline(
                updateDTO.getDeadline());

        savedClass.setLocation(
                updateDTO.getLocation());

        int result =
                classMapper.updateClass(
                        savedClass);

        if (result != 1) {
            throw new IllegalStateException(
                    "클래스 수정 처리 중 오류가 발생했습니다."
            );
        }

        // 수정된 날짜를 기준으로 상태 재계산
        classMapper.updateClassStatus(
                updateDTO.getClassId());
    }


    // =========================================
    // 클래스 삭제
    // =========================================

    @Override
    @Transactional
    public void deleteClass(
            Long classId,
            Long loginMemberId) {

        ClassVO savedClass =
                classMapper.selectClassById(
                        classId);

        if (savedClass == null) {
            throw new IllegalArgumentException(
                    "존재하지 않는 클래스입니다."
            );
        }

        // 개설자 권한 확인
        if (!savedClass.getHostId()
                .equals(loginMemberId)) {

            throw new SecurityException(
                    "클래스를 삭제할 권한이 없습니다."
            );
        }

        int result =
                classMapper.deleteClass(
                        classId);

        if (result != 1) {
            throw new IllegalStateException(
                    "클래스 삭제 처리 중 오류가 발생했습니다."
            );
        }
    }
}