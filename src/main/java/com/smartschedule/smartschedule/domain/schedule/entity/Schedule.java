package com.smartschedule.smartschedule.domain.schedule.entity;

import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.schedule.enums.Priority;
import com.smartschedule.smartschedule.domain.schedule.enums.ScheduleType;
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
@Table(name = "schedule")
public class Schedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Priority priority;

    @Column(nullable = false)
    private boolean isCompleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private ScheduleType scheduleType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Builder
    public Schedule(
            String title,
            String content,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Priority priority,
            boolean isCompleted,
            ScheduleType scheduleType,
            Member member,
            Category category
    ) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.isCompleted = isCompleted;
        this.scheduleType = scheduleType;
        this.member = member;
        this.category = category;
    }

    // 일정 정보 수정
    public void update(
            String title,
            String content,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Priority priority,
            ScheduleType scheduleType,
            Category category
    ) {
        this.title = title;
        this.content = content;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.scheduleType = scheduleType;
        this.category = category;
    }

    // 상태 변경
    public void updateStatus(boolean isCompleted) {
        this.isCompleted = isCompleted;
    }
}
