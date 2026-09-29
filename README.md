# OneDay Class

원데이 클래스 등록, 조회, 신청 및 리뷰 기능을 제공하는 웹 서비스입니다.

## 주요 기능

- 회원가입 및 로그인
- 원데이 클래스 등록 및 조회
- 클래스 신청
- 클래스 리뷰 및 댓글
- 이미지 첨부 및 AWS S3 업로드
- Kakao Map API를 이용한 위치 정보 제공
- 관리자 기능

## 기술 스택

### Backend
- Java
- Spring Boot
- Spring Security
- Gradle

### Database
- MySQL

### Frontend
- HTML
- CSS
- JavaScript
- Thymeleaf

### Cloud / API
- AWS S3
- Kakao Map API

## 프로젝트 구조

```text
src
 └─ main
     ├─ java
     │   └─ kr.or.oti.onedayclass
     │       ├─ config
     │       ├─ controller
     │       ├─ dto
     │       ├─ exception
     │       ├─ mapper
     │       ├─ scheduler
     │       ├─ security
     │       ├─ service
     │       └─ vo
     │
     └─ resources
         ├─ mapper
         ├─ static
         ├─ templates
         └─ application.properties
