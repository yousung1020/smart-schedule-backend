package com.smartschedule.smartschedule.domain.notification.service;

import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.notification.entity.Notification;
import com.smartschedule.smartschedule.domain.notification.repository.NotificationRepository;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.global.util.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleNotificationServiceTest {
    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private ScheduleNotificationService scheduleNotificationService;

    private Notification testNotification;

    @BeforeEach
    void setUp() {
        Member member = Member.builder()
                .email("test@domain.com")
                .nickname("Tester")
                .password("pw")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(member, "id", 1L);

        Schedule schedule = Schedule.builder()
                .title("테스트 일정")
                .content("테스트 내용")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(1))
                .member(member)
                .build();
        ReflectionTestUtils.setField(schedule, "id", 10L);

        testNotification = Notification.builder()
                .schedule(schedule)
                .notifyAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(testNotification, "id", 100L);
    }

    @Test
    @DisplayName("스케줄러 실행 - 발송 대상이 없는 경우 메일 발송 호출 안됨")
    void processNotifications_EmptyList() {
        // given
        when(notificationRepository.findPendingNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        scheduleNotificationService.processNotifications();

        // then
        verify(mailService, never()).sendNotificationEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("스케줄러 실행 - 발송 대상이 있는 경우 메일 발송 후 isSent 상태 true 변경 검증")
    void processNotifications_WithPendingList() {
        // given
        when(notificationRepository.findPendingNotifications(any(LocalDateTime.class)))
                .thenReturn(List.of(testNotification));

        // when
        scheduleNotificationService.processNotifications();

        // then
        // 메일 서비스가 1번 호출되었는지 검증
        verify(mailService, times(1)).sendNotificationEmail(
                eq("test@domain.com"),
                anyString(),
                anyString()
        );
        
        // 도메인 엔티티의 상태(더티 체킹용)가 true로 변경되었는지 단언
        assertTrue(testNotification.getIsSent());
    }
}
