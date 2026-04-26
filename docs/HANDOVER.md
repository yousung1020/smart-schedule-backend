# Smart Schedule 프로젝트 인수인계 문서 (Handover)

## 1. 프로젝트 개요 및 현재 상태
본 문서는 **Smart Schedule** 백엔드 개발의 1단계(인증 및 인프라 구축) 완료 후, 다음 세션에서 원활한 개발을 이어가기 위해 작성되었습니다. 현재 핵심 인증 파이프라인과 보안 인프라가 완벽하게 구축 및 검증된 상태입니다.

---

## 2. 기구축된 핵심 아키텍처 및 기능

### 2.1. 인프라 및 보안 (Infrastructure & Security)
- **기술 스택**: Java 21, Spring Boot 3.4.x, JPA, QueryDSL, Redis, MySQL.
- **Spring Security**: 무상태(Stateless) JWT 인증 체계 구축.
- **토큰 관리 전략**:
    - **Access Token**: 클라이언트가 `Authorization: Bearer` 헤더로 관리.
    - **Refresh Token**: 브라우저의 **HttpOnly, SameSite=Lax** 쿠키에 저장하여 XSS/CSRF 방어.
    - **RTR (Refresh Token Rotation)**: 재발급 시 기존 RT를 폐기하고 새로운 RT를 발급하여 보안 강화.
    - **로그아웃**: Access Token을 Redis 블랙리스트에 등록(남은 유효시간만큼)하고 RT를 삭제.
- **Global Payload**: 모든 응답은 `ApiResponse<T>` 규격으로 통일, `GeneralExceptionAdvice`를 통한 전역 예외 처리.

### 2.2. 인증 도메인 (Auth Domain)
- **카카오 소셜 로그인**: **Authorization Code Grant(인가 코드 방식)** 적용.
    - 프론트엔드로부터 인가 코드를 받아 백엔드에서 카카오 토큰 교환 및 유저 정보를 조회하는 표준 방식 준수.
- **관심사 분리 (SRP)**: 외부 API 통신을 전담하는 `KakaoOAuthClient` 분리.
- **확장성 (Scalability)**: `OAuth2UserInfo` 인터페이스 도입을 통해 향후 구글, 네이버 등 신규 제공자 추가 시 `AuthService` 코드 수정 최소화.
- **안정성**: 
    - `RestClient` 빈 등록 및 타임아웃(Connect 3s, Read 5s) 설정.
    - 만료된 토큰이더라도 서명이 유효하면 로그아웃을 허용하는 `getClaimsForLogout` 로직 적용.

### 2.3. 회원 도메인 (Member Domain)
- **CQRS 패턴**: `MemberCommandService`(생성/수정)와 `MemberQueryService`(조회)를 엄격히 분리.
- **데이터 무결성**: `socialProvider` + `socialId` 복합 UNIQUE 제약 조건 적용.
- **동시성 방어**: 회원가입 시 발생할 수 있는 Race Condition을 `DataIntegrityViolationException` 핸들링으로 방어.

---

## 3. 핵심 비즈니스 로직 단위 테스트 완료
- **테스트 케이스 (`AuthServiceTest`)**:
    1. 소셜 로그인 신규 가입 및 토큰 발급 성공 검증.
    2. **만료된 토큰**을 사용한 로그아웃 성공 시나리오 검증.
    3. **RTR 동시성 방어**: 동일한 리프레시 토큰으로 동시 요청 시 2회차 요청 즉시 차단 (Redis `getAndDelete` 원자적 연산 활용).

---

## 4. 다음 세션 작업 예정 사항 (Next Steps)

### 4.1. 카테고리 (Category) 도메인 개발
- 사용자가 자신의 일정을 분류할 수 있는 카테고리 CRUD 개발.
- 유저별 커스텀 카테고리 생성 로직.

### 4.2. 일정 (Schedule) 도메인 개발
- 메인 기능인 일정 생성, 조회, 수정, 삭제(Soft Delete).
- QueryDSL을 활용한 기간별/카테고리별 동적 필터링 검색.

### 4.3. 알림 및 통계
- Spring Scheduler를 활용한 시작 1시간 전 이메일 알림.
- 완료율 및 카테고리별 활동량 통계 API.

### 4.4. 인프라 확장
- Nginx 도입: 프론트엔드와 백엔드의 리버스 프록시 및 SSL 설정 예정.
- Docker Compose 서비스 추가.

---

## 5. 실행 가이드
1. **인프라 기동**: `smart-schedule-backend` 폴더에서 `docker-compose up -d` 실행 (MySQL, Redis).
2. **서버 실행**: `./gradlew bootRun`.
3. **API 명세**: 서버 실행 후 `http://localhost:8080/swagger-ui/index.html` 접속.
