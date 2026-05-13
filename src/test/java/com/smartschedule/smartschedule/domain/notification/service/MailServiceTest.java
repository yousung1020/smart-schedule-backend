package com.smartschedule.smartschedule.domain.notification.service;

import com.smartschedule.smartschedule.global.util.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {
    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        // @Value 로 주입받는 필드 강제 세팅
        ReflectionTestUtils.setField(mailService, "fromAddress", "test@test.com");
    }

    @Test
    @DisplayName("이메일 발송 성공 - 재시도 없이 1번만 호출되어야 함")
    void sendNotificationEmail_Success() {
        // given
        String to = "user@test.com";
        String subject = "테스트 제목";
        String text = "테스트 내용";

        // when
        mailService.sendNotificationEmail(to, subject, text);

        // then
        verify(javaMailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    @DisplayName("이메일 발송 실패 - 예외 발생 시 최대 3번까지 재시도해야 함")
    void sendNotificationEmail_Retry_Fail() {
        // given
        String to = "user@test.com";
        String subject = "테스트 제목";
        String text = "테스트 내용";

        // 메일 발송 시 강제로 예외 발생 모킹
        doThrow(new MailSendException("SMTP 서버 응답 없음"))
                .when(javaMailSender).send(any(SimpleMailMessage.class));

        // when
        mailService.sendNotificationEmail(to, subject, text);

        // then
        // 최대 재시도 횟수(3회)만큼 호출되었는지 검증
        verify(javaMailSender, times(3)).send(any(SimpleMailMessage.class));
    }
}
