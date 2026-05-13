package com.smartschedule.smartschedule.domain.notification.repository;

import com.smartschedule.smartschedule.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // 발송할 시간이 되었고 아직 보내지 않은 알림 조회 (N+1 방지를 위해 Schedule과 Member 페치 조인)
    @Query("SELECT n FROM Notification n " +
            "JOIN FETCH n.schedule s " +
            "JOIN FETCH s.member m " +
            "WHERE n.notifyAt <= :now AND n.isSent = false AND m.isActive = true")
    List<Notification> findPendingNotifications(@Param("now") LocalDateTime now);

    // 일정 삭제 시 관련 알림도 삭제 (단일 쿼리 벌크 연산을 통한 SELECT N+1 방지)
    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.schedule.id = :scheduleId")
    void deleteBulkByScheduleId(@Param("scheduleId") Long scheduleId);
}
