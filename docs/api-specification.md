# [API 명세서] Smart Scheduler API v1.1

## 1. 인증 API
- POST /api/v1/auth/signup : 회원가입 신청
- POST /api/v1/auth/login : 로그인 및 토큰 발급

## 2. 일정 API
- GET /api/v1/schedules : 조건별 일정 목록 검색 (검색어, 기간, 카테고리 필터)
- POST /api/v1/schedules : 신규 일정 등록
- GET /api/v1/schedules/{id} : 일정 상세 내역 조회
- PATCH /api/v1/schedules/{id}/status : 일정 진행 상태 변경 (완료 여부 등)
- DELETE /api/v1/schedules/{id} : 일정 삭제 처리 (is_deleted 필드 업데이트)

## 3. 통계 API (Statistics)
- GET /api/v1/stats/completion-rate : 특정 기간 내 일정 완료율 조회
- GET /api/v1/stats/category-distribution : 카테고리별 일정 점유율 데이터 조회
- GET /api/v1/stats/weekly-activity : 최근 4주간의 주차별 활동량 통계 조회

## 4. 카테고리 API
- GET /api/v1/categories : 본인이 생성한 카테고리 목록 조회
- POST /api/v1/categories : 커스텀 카테고리 추가ß