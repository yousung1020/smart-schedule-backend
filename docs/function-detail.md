# 기능별 상세 구현 내역

## 1. 회원 및 인증 인가 체계
- 비밀번호 암호화: Spring Security의 BCryptPasswordEncoder를 통한 단방향 해시 적용
- JWT 발급: 로그인 성공 시 Access Token 및 Refresh Token 페어 생성 및 반환
- 무상태 인증: 세션을 사용하지 않고 JWT 기반의 커스텀 필터를 SecurityFilterChain에 등록하여 토큰 검증
- 인가 처리: @AuthenticationPrincipal 어노테이션을 활용하여 컨트롤러 계층에서 현재 로그인한 사용자 식별

## 2. 일정 관리 (CRUD 및 조회 성능 최적화)
- 엔티티 매핑: 일정과 회원, 일정과 카테고리 간의 다대일 연관관계 및 지연 로딩 설정
- 데이터 전달 객체 분리: 엔티티 직접 노출 방지를 위한 Request DTO 및 Response DTO 생성
- 예외 처리: @RestControllerAdvice를 활용한 전역 예외 처리 및 표준 에러 응답 객체 반환
- 동적 검색: QueryDSL을 활용하여 기간, 카테고리, 검색어 등 다양한 조건에 대한 동적 쿼리 작성 및 N+1 문제 해결

## 3. 이메일 알림 시스템 (비동기 및 스케줄링)
- 스케줄러 활성화: 메인 애플리케이션 클래스에 @EnableScheduling 어노테이션 적용
- 대상 추출: @Scheduled 어노테이션을 활용하여 매분마다 시작 시간이 1시간 남은 일정 목록 데이터베이스 스캔
- 비동기 처리 활성화: @EnableAsync 어노테이션 적용 및 커스텀 스레드 풀 설정
- 발송 로직 분리: 이메일 전송 메서드에 @Async 어노테이션을 적용하여 메인 스레드 대기 시간 방지
- 외부 연동: spring-boot-starter-mail 의존성 추가 및 외부 SMTP 서버 연동

## 4. 통계 대시보드 (데이터 집계)
- 완료율 산출: 특정 기간 내 전체 일정 수 대비 상태가 완료인 일정 수 비율 계산 쿼리 작성
- 카테고리별 집계: GROUP BY 구문을 활용한 카테고리별 일정 개수 산출 및 DTO 직접 조회 성능 최적화
- 통계 캐싱: 데이터 변동이 적은 지난달 통계 등은 @Cacheable을 활용하여 인메모리 캐시 적용 및 데이터베이스 부하 감소

## 5. 인프라 구축 및 API 문서화
- 컨테이너화: Dockerfile 작성 및 스프링 부트 애플리케이션 이미지 빌드
- 통합 실행: docker-compose.yml 파일을 통한 애플리케이션 및 MySQL 데이터베이스 동시 실행 환경 구성
- API 명세: springdoc-openapi-starter-webmvc-ui 의존성 추가를 통한 Swagger UI 자동 렌더링 및 프론트엔드 개발자 제공