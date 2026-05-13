package com.smartschedule.smartschedule.domain.notification.service;

import com.smartschedule.smartschedule.domain.notification.entity.Notification;
import com.smartschedule.smartschedule.domain.notification.repository.NotificationRepository;
import com.smartschedule.smartschedule.global.util.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleNotificationService {
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    // application.yaml의 설정값에 따라 스케줄러 동작
    @Scheduled(cron = "${notification.scheduler.cron}")
    @Transactional
    public void processNotifications() {
        LocalDateTime now = LocalDateTime.now();
        
        // 발송 대기 중인 알림 조회 (N+1 방지 쿼리)
        List<Notification> pendingNotifications = notificationRepository.findPendingNotifications(now);

        if (pendingNotifications.isEmpty()) {
            return;
        }

        log.info("{}개의 대기 중인 알림을 처리합니다.", pendingNotifications.size());

        for (Notification notification : pendingNotifications) {
            String to = notification.getSchedule().getMember().getEmail();
            String title = notification.getSchedule().getTitle();
            String message = String.format("알림: '%s' 일정이 곧 시작됩니다.", title);
            
            // 비동기로 이메일을 발송
            mailService.sendNotificationEmail(to, "[Smart Schedule] 일정 알림", message);
            
            // 영속성 컨텍스트 내의 엔티티 상태 변경 
            notification.markAsSent();
        }
    }
}
