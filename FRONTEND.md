# 하루클래스 프론트엔드

기존 Spring MVC 컨트롤러, 서비스, DTO/VO, Mapper, SQL, Security 설정은 변경하지 않았습니다.

## 파일 구성

- `src/main/resources/templates/fragments/layout.html`: 공통 head, 검색 헤더, 인증 메뉴, 푸터
- `src/main/resources/templates/fragments/components.html`: 카테고리, 클래스 카드, 상태 배지, CSS 썸네일
- `src/main/resources/templates/`: 기존 13개 화면을 동일한 디자인으로 정리
- `src/main/resources/static/css/app.css`: 색상, 컴포넌트, 반응형 레이아웃
- `src/main/resources/static/css/vendor/bootstrap.min.css`: Bootstrap 5.3.3 (MIT), 로컬 제공
- `src/main/resources/static/js/app.js`: 모바일 메뉴, 작업 확인, 날짜 검증, 메인 최신 클래스 표시

## 화면 연결

기존 애플리케이션을 실행하고 `/`로 접속합니다. Thymeleaf 템플릿은 HTML 파일을 직접 열지 말고 서버를 통해 확인하세요.
STS에서 변경이 반영되지 않으면 프로젝트 새로고침/빌드 후 애플리케이션을 다시 시작하고 브라우저를 강력 새로고침하세요.

메인 컨트롤러에 목록 데이터가 없으므로 메인 JavaScript가 기존 공개 경로 `/class/list?size=4`를 GET으로 요청합니다.
응답 HTML의 `#class-results .class-card`에서 최신 카드 4개를 가져옵니다. 새 API나 컨트롤러는 추가하지 않았습니다.
카드 렌더링은 메인과 목록에서 같은 Thymeleaf fragment를 사용합니다.
데이터 없음, 요청 실패, JavaScript 비활성 상태에는 안내와 목록 이동 경로를 제공합니다.

썸네일은 외부 이미지나 업로드 없이 카테고리별 CSS 도형으로 표현합니다.
가격 0원은 '무료', 가격은 천 단위 쉼표로 표시합니다.
클래스 상태와 개인 신청 상태의 라벨을 별도로 유지합니다.
기존 POST 경로, 파라미터, th:object/th:field, 서버 검증 오류, Spring Security CSRF 자동 삽입을 유지합니다.

## 반응형

- 1100px 초과: 클래스 카드 4열
- 601~1100px: 카드 2열
- 600px 이하: 카드 1열, 접이식 메뉴, 카테고리 2열, 입력 폼 1열
- 850px 이하: 상세 화면을 이미지 → 소개 → 신청 정보 → 댓글·후기로 배치
- 마이페이지 표는 모바일에서 항목별 라벨이 있는 카드 형태로 전환

## 확인 범위

실제 프로젝트 버전의 Thymeleaf/Spring Security를 사용하는 임시 검증기로 32개 렌더링 시나리오를 확인했습니다.
검증기는 프로젝트 밖 임시 폴더에 두었으며 DB를 사용하지 않았습니다.
화면 13개, 익명/회원/개설자 및 신청 상태, 입력 오류, CSRF 필드, 날짜 입력 형식, 페이지 번호 묶음을 검사했습니다.
브라우저에서 390px, 768px, 1366px 너비로 모든 화면의 가로 넘침 여부와 카드 열 수를 확인했습니다.
실제 DB의 회원가입·신청·승인·삭제를 실행하는 통합 테스트는 수행하지 않았습니다.
