package com.smartschedule.smartschedule.domain.schedule.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Priority {
    HIGH("높음", 3),
    MEDIUM("중간", 2),
    LOW("낮음", 1);

    private final String description;
    private final int level;
}
