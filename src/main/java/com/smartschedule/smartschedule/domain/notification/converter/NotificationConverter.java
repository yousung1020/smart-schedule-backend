package com.smartschedule.smartschedule.domain.notification.converter;

import com.smartschedule.smartschedule.domain.notification.entity.Notification;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;

import java.time.LocalDateTime;
import java.util.List;

public class NotificationConverter {

    public static List<Notification> toEntities(Schedule schedule, List<Integer> notifyBeforeMinutes) {
        if (notifyBeforeMinutes == null || notifyBeforeMinutes.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        LocalDateTime now = LocalDateTime.now();

        return notifyBeforeMinutes.stream()
                .map(minutes -> schedule.getStartTime().minusMinutes(minutes))
                .filter(notifyTime -> notifyTime.isAfter(now)) // 이미 지나간 과거 시간의 알림은 무시
                .map(notifyTime -> Notification.builder()
                        .schedule(schedule)
                        .notifyAt(notifyTime)
                        .build())
                .toList();
    }
}
