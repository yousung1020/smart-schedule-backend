package com.smartschedule.smartschedule.global.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    private static final int MAX_RETRIES = 3;

    @Async
    public void sendNotificationEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                javaMailSender.send(message);
                log.info("이메일 발송 성공 - To: {}", to);
                return;
            } catch (Exception e) {
                log.warn("이메일 발송 실패 (시도 {}/{}) - To: {}, 원인: {}", attempt, MAX_RETRIES, to, e.getMessage());

                if (attempt == MAX_RETRIES) {
                    log.error("최대 재시도 횟수 초과. 이메일 발송 최종 실패 - To: {}", to, e);
                } else {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.error("이메일 재시도 대기 중 인터럽트 발생 - To: {}", to);
                        return;
                    }
                }
            }
        }
    }

    public void sendPasswordResetMail(String to, String resetLink) {
        String subject = "[Smart Schedule] 비밀번호 재설정 안내";
        String text = """
                안녕하세요. Smart Schedule입니다.

                비밀번호를 재설정하려면 아래 링크를 클릭해주세요.
                링크는 15분 동안 유효합니다.

                %s

                본인이 요청하지 않은 경우 이 메일을 무시하셔도 됩니다.
                """.formatted(resetLink);
        
        sendNotificationEmail(to, subject, text);
    }
}
