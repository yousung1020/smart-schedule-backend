package com.smartschedule.smartschedule.domain.notification.entity;

import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "notification")
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @Column(nullable = false)
    private LocalDateTime notifyAt;

    @Column(nullable = false)
    private Boolean isSent;

    @Builder
    public Notification(Schedule schedule, LocalDateTime notifyAt) {
        this.schedule = schedule;
        this.notifyAt = notifyAt;
        this.isSent = false; // 신규 생성 시 기본값 설정
    }

    // 알림 발송 완료 상태로 변경
    public void markAsSent() {
        this.isSent = true;
    }
}