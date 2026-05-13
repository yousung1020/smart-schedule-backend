package com.smartschedule.smartschedule.domain.schedule.repository;

import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long>, ScheduleRepositoryCustom {
    Optional<Schedule> findByIdAndMemberId(Long id, Long memberId);

    // 특정 회원이 소유한 특정 일정 존재 여부 확인
    boolean existsByIdAndMemberId(Long id, Long memberId);

    // 카테고리 삭제 전, 해당 카테고리를 참조하는 일정들의 연결을 해제(NULL)
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Schedule s SET s.category = null WHERE s.category.id = :categoryId")
    void setCategoryNullByCategoryId(@Param("categoryId") Long categoryId);

    // 시작 시간이 특정 범위에 포함되고 상태가 특정 값인 일정 조회
    // 알림 발송용으로, N+1 방지를 위해 Member 페치 조인 수행 및 탈퇴하지 않은 회원만 필터링
    @Query("SELECT s FROM Schedule s JOIN FETCH s.member m WHERE s.isCompleted = :isCompleted AND m.isActive = true AND s.startTime >= :start AND s.startTime < :end")
    List<Schedule> findUpcomingSchedules(
            @Param("isCompleted") boolean isCompleted,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
