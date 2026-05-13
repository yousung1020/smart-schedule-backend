package com.smartschedule.smartschedule.domain.schedule.service.command;

import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.category.service.query.CategoryQueryService;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.notification.service.command.NotificationCommandService;
import com.smartschedule.smartschedule.domain.schedule.converter.ScheduleConverter;
import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.dto.response.ScheduleResponseDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.domain.schedule.event.ScheduleChangedEvent;
import com.smartschedule.smartschedule.domain.schedule.exception.ScheduleException;
import com.smartschedule.smartschedule.domain.schedule.exception.code.error.ScheduleErrorCode;
import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import com.smartschedule.smartschedule.domain.schedule.service.query.ScheduleQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleCommandService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleQueryService scheduleQueryService;
    private final MemberRepository memberRepository;
    private final CategoryQueryService categoryQueryService;
    private final NotificationCommandService notificationCommandService;
    private final ApplicationEventPublisher eventPublisher;

    // 일정 생성
    public ScheduleResponseDTO.ScheduleResultDTO createSchedule(
            ScheduleRequestDTO.ScheduleCreateDTO request,
            Long memberId
    ) {
        log.info("일정 생성을 시작합니다: memberId={}, title={}", memberId, request.title());
        validateScheduleDates(request.startTime(), request.endTime());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Category category = resolveCategory(request.categoryId(), memberId);

        Schedule schedule = ScheduleConverter.toEntity(request, member, category);
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 사용자가 요청한 옵션에 따라 알림 다건 생성
        notificationCommandService.createNotifications(savedSchedule, request.notifyBeforeMinutes());

        // 일정 변경 이벤트 발행 (캐시 무효화 등 처리)
        eventPublisher.publishEvent(new ScheduleChangedEvent(memberId));

        log.info("일정이 성공적으로 생성되었습니다: scheduleId={}, memberId={}", savedSchedule.getId(), memberId);
        return ScheduleConverter.toResultDTO(savedSchedule);
    }

    // 일정 수정
    public ScheduleResponseDTO.ScheduleResultDTO updateSchedule(
            Long scheduleId,
            ScheduleRequestDTO.ScheduleUpdateDTO request,
            Long memberId
    ) {
        log.info("일정 수정을 시작합니다: scheduleId={}, memberId={}, newTitle={}", scheduleId, memberId, request.title());
        validateScheduleDates(request.startTime(), request.endTime());

        Schedule schedule = scheduleQueryService.findByIdAndMemberId(scheduleId, memberId);
        Category category = resolveCategory(request.categoryId(), memberId);

        schedule.update(
                request.title(),
                request.content(),
                request.startTime(),
                request.endTime(),
                request.priority(),
                request.scheduleType(),
                category
        );

        // 기존 알림 전체 삭제 후, 사용자가 요청한 옵션으로 새롭게 생성
        notificationCommandService.updateNotifications(schedule, request.notifyBeforeMinutes());

        // 일정 변경 이벤트 발행
        eventPublisher.publishEvent(new ScheduleChangedEvent(memberId));

        log.info("일정이 성공적으로 수정되었습니다: scheduleId={}, memberId={}", scheduleId, memberId);
        return ScheduleConverter.toResultDTO(schedule);
    }

    // 일정 완료 상태 변경
    public ScheduleResponseDTO.ScheduleResultDTO updateScheduleCompletion(
            Long scheduleId,
            boolean isCompleted,
            Long memberId
    ) {
        log.info("일정 완료 상태 변경을 시작합니다: scheduleId={}, memberId={}, isCompleted={}", scheduleId, memberId, isCompleted);
        Schedule schedule = scheduleQueryService.findByIdAndMemberId(scheduleId, memberId);
        schedule.updateStatus(isCompleted);

        // 일정 변경 이벤트 발행
        eventPublisher.publishEvent(new ScheduleChangedEvent(memberId));

        log.info("일정 완료 상태가 변경되었습니다: scheduleId={}, isCompleted={}", scheduleId, isCompleted);
        return ScheduleConverter.toResultDTO(schedule);
    }

    // 일정 삭제
    public void deleteSchedule(
            Long scheduleId,
            Long memberId
    ) {
        log.info("일정 삭제를 시작합니다: scheduleId={}, memberId={}", scheduleId, memberId);
        Schedule schedule = scheduleQueryService.findByIdAndMemberId(scheduleId, memberId);
        
        // 연관된 알림 모두 삭제
        notificationCommandService.deleteNotificationsByScheduleId(scheduleId);
        
        scheduleRepository.delete(schedule);

        // 일정 변경 이벤트 발행
        eventPublisher.publishEvent(new ScheduleChangedEvent(memberId));

        log.info("일정이 성공적으로 삭제되었습니다: scheduleId={}, memberId={}", scheduleId, memberId);
    }

    private Category resolveCategory(
            Long categoryId,
            Long memberId
    ) {
        if (categoryId == null) {
            return null;
        }
        return categoryQueryService.findByIdAndMemberId(categoryId, memberId);
    }

    private void validateScheduleDates(
            LocalDateTime startTime,
            LocalDateTime endTime
    ) {
        if (startTime == null || endTime == null) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_SCHEDULE_DATE);
        }
        
        if (endTime.isBefore(startTime)) {
            throw new ScheduleException(ScheduleErrorCode.INVALID_SCHEDULE_DATE);
        }
    }
}
