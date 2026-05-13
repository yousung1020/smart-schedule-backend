package com.smartschedule.smartschedule.domain.schedule.event;

/**
 * 일정 변경 시 발생하는 이벤트
 * @param memberId 일정이 변경된 사용자의 식별자
 */
public record ScheduleChangedEvent(Long memberId) {}
