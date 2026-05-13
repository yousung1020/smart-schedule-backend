package com.smartschedule.smartschedule.domain.schedule.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ScheduleType {
    START("시작"),
    DEADLINE("마감");

    private final String description;
}
