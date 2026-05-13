package com.smartschedule.smartschedule.domain.notification.service.command;

import com.smartschedule.smartschedule.domain.notification.converter.NotificationConverter;
import com.smartschedule.smartschedule.domain.notification.entity.Notification;
import com.smartschedule.smartschedule.domain.notification.repository.NotificationRepository;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {

    private final NotificationRepository notificationRepository;

    // 사용자가 요청한 분 기준으로 리스트를 기반으로 알림 생성
    public void createNotifications(Schedule schedule, List<Integer> notifyBeforeMinutes) {
        List<Notification> notifications = NotificationConverter.toEntities(schedule, notifyBeforeMinutes);

        if (!notifications.isEmpty()) {
            notificationRepository.saveAll(notifications);
        }
    }

    // 일정 수정 시 기존 알림 일괄 삭제 후, 사용자가 넘긴 새 옵션으로 재생성
    public void updateNotifications(Schedule schedule, List<Integer> notifyBeforeMinutes) {
        notificationRepository.deleteBulkByScheduleId(schedule.getId());
        createNotifications(schedule, notifyBeforeMinutes);
    }

    // 일정 삭제 시 연관된 알림 모두 삭제 (외래 키 무결성 보장)
    public void deleteNotificationsByScheduleId(Long scheduleId) {
        notificationRepository.deleteBulkByScheduleId(scheduleId);
    }
}
