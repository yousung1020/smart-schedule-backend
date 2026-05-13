# Smart Schedule 프로젝트 인수인계 문서 (HANDOVER 3 - 알림 시스템 Phase)

## 1. 프로젝트 개요 및 현재 상태
본 문서는 **Smart Schedule** 백엔드 개발의 3단계(이메일 알림 시스템 구축) 진행 중 작성되었습니다. 
현재 컨텍스트 정리를 위해 세션을 분리하며, 다음 세션에서 원활한 개발을 이어가기 위한 인수인계 목적입니다.

---

## 2. 현재 세션 완료 작업 요약 (Phase 3 진행 중)

### 2.1. 알림 시스템 (Notification) 기반 아키텍처 설계 및 구현
- **유연한 1:N 구조 적용**: 일정(Schedule)과 알림(Notification)을 1:N 구조로 분리하여, 사용자가 원하는 다양한 시간대(예: 1시간 전, 30분 전)에 다중 알림을 받을 수 있도록 확장성 있는 엔티티 설계 (`Notification` 엔티티 생성 완료).
- **데이터베이스 최적화**: 
  - `NotificationRepository`에 페치 조인(`JOIN FETCH`)을 적용하여 일정 및 회원 정보를 조회 시 N+1 문제를 방지.
  - 탈퇴한 회원에게 메일이 발송되지 않도록 쿼리 내 `m.isActive = true` 조건 추가.
  - 일정 삭제/수정 시 기존 알림 데이터를 지우기 위해 영속성 컨텍스트를 고려한 벌크 삭제(`@Modifying(flushAutomatically = true)`) 로직 구현 완료.

### 2.2. 알림 도메인 비즈니스 로직 연동
- **Converter 패턴 적용**: 비즈니스 로직의 응집도를 높이기 위해 알림 생성 시 시간 계산 및 엔티티 변환 로직을 `NotificationConverter`로 분리.
- **서비스 분리 (단일 책임 원칙)**: `NotificationCommandService`를 별도로 생성하여, `ScheduleCommandService` 내부에서 알림 데이터를 관리하도록 책임을 분리하고 연동 완료.
- **DTO 업데이트**: `ScheduleCreateDTO`, `ScheduleUpdateDTO`에 `List<Integer> notifyBeforeMinutes` 필드를 추가하여 클라이언트로부터 다중 알림 옵션을 받을 수 있도록 수정.

### 2.3. 비동기 및 스케줄링 인프라 설정
- `@EnableAsync` 및 `@EnableScheduling` 활성화를 위한 `AsyncConfig`, `SchedulingConfig` 분리 생성 완료.
- **예외 처리**: 비동기 스레드 내 Silent Failure 방지를 위해 `AsyncUncaughtExceptionHandler` 커스텀 핸들러 등록.
- **환경 변수 주입**: `application.yaml`에 이메일 서버(SMTP) 설정 및 비동기 풀 사이즈, 스케줄러 Cron 표현식 추가 완료.

### 2.4. 테스트 코드 작성 및 검증
- `ScheduleCommandServiceTest`에 알림 연동 관련 Mock 테스트 코드 작성 완료 및 통과 확인 (`./gradlew clean test --tests *ScheduleCommandServiceTest*`).

---

## 3. 발생한 문제점 (Troubleshooting)

- **전체 빌드 테스트 실패 (`./gradlew clean test`)**: 
  `@SpringBootTest`가 붙은 보일러플레이트 테스트(`SmartscheduleApplicationTests`)가 전체 스프링 컨텍스트를 로드하려 시도하면서, 환경 변수로 주입받아야 할 DB 접속 정보(`MYSQL_DATABASE_URL` 등)가 누락되어 DB 연결 예외(HibernateException)가 발생했습니다. (순수 단위 테스트는 통과하였으나, 통합 컨텍스트 테스트 환경 설정 누락으로 빌드 실패)

---

## 4. 다음 세션 작업 예정 사항 (Next Steps)

다음 세션에서는 새로운 컨텍스트로 아래 작업들을 순차적으로 이어가시면 됩니다.

### 4.1. 남은 알림 시스템(Phase 3) 구현 마무리
- **`MailService` 작성**: `JavaMailSender`를 활용하여 비동기(`@Async`)로 실제 이메일을 발송하는 서비스 구현 (코드 리뷰까지 완료되었으나 파일 생성 전).
- **`ScheduleNotificationService` 작성**: `@Scheduled`를 통해 매 분마다 `NotificationRepository`를 폴링하여 발송 대상 알림을 조회하고 `MailService`에 위임한 뒤, 상태를 발송 완료(`isSent = true`)로 변경하는 오케스트레이터 로직 구현 (코드 리뷰 완료, 파일 생성 전).

### 4.2. 테스트 환경(CI/CD 대비) 안정화
- 전체 단위/통합 테스트를 안정적으로 수행하기 위해, 테스트용 인메모리 DB(H2) 설정(`application-test.yaml`)을 추가하고 기존 테스트 클래스들의 프로파일 설정을 점검하여 `./gradlew clean test`가 정상적으로 통과하도록 빌드 스크립트 보완.

### 4.3. 통계 대시보드 (Statistics Dashboard - Phase 4)
- 알림 시스템 마무리 후, 기획된 일정 완료율, 카테고리 비중 등의 통계 조회 API 구현 시작.