# [기능 명세서] Smart Scheduler

## 1. 회원 및 인증 관리
- JWT 기반 보안 인증 체계 구축 및 토큰 만료 처리
- 소셜 로그인이 메인인 인증 서비스(OAuth2.0)
- 회원가입 시 비밀번호 암호화 및 유효성 검증 로직 적용(선택)

## 2. 일정 관리 (CRUD & Advanced)
- 일정의 생성, 조회, 수정, 삭제(Soft Delete) 기능
- 제목 및 내용 기반의 키워드 검색 및 기간별 필터링
- 중요도(Priority) 및 진행 상태(Status) 관리

## 3. 비동기 알림 시스템 (Notification)
- Spring @Scheduled를 활용한 알림 대상 스케줄링
- @Async 기반의 비동기 이메일 발송 처리로 성능 최적화
- 발송 실패 시 로그 기록 및 재시도 전략 수립

## 4. 통계 대시보드 (Statistics)
- 월간 일정 완료율 및 달성도 계산 기능
- 카테고리별 일정 분포도 집계 및 시각화 데이터 제공
- 주차별 활동량 추이 분석 데이터 산출
- JPQL 또는 QueryDSL을 활용한 복잡한 통계 쿼리 최적화

## 5. 인프라 및 품질
- Docker Compose를 활용한 앱과 DB의 컨테이너화
- Swagger(Springdoc)를 이용한 API 문서 자동 생성