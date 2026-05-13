package com.smartschedule.smartschedule.domain.schedule.service.query;

import com.smartschedule.smartschedule.domain.schedule.converter.ScheduleConverter;
import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.dto.response.ScheduleResponseDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.domain.schedule.exception.ScheduleException;
import com.smartschedule.smartschedule.domain.schedule.exception.code.error.ScheduleErrorCode;
import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleQueryService {

    private final ScheduleRepository scheduleRepository;

    // 단건 상세 조회 (보안 최적화 적용)
    public ScheduleResponseDTO.ScheduleResultDTO getScheduleDetail(Long scheduleId, Long memberId) {
        log.info("일정 상세 정보를 조회합니다: scheduleId={}, memberId={}", scheduleId, memberId);
        Schedule schedule = findByIdAndMemberId(scheduleId, memberId);
        return ScheduleConverter.toResultDTO(schedule);
    }

    // 캘린더 뷰 (페이징 없는 전체 리스트)
    public ScheduleResponseDTO.CalendarListResultDTO getCalendarSchedules(
            Long memberId,
            ScheduleRequestDTO.CalendarSearch condition
    ) {
        log.info("캘린더 일정을 조회합니다: memberId={}, range={} ~ {}", memberId, condition.startDate(), condition.endDate());
        List<Schedule> schedules = scheduleRepository.findCalendarSchedules(memberId, condition);
        return ScheduleConverter.toCalendarListResultDTO(schedules);
    }

    // 리스트 뷰 (동적 필터 및 페이징 적용)
    public ScheduleResponseDTO.PagedListResultDTO searchSchedules(
            Long memberId,
            ScheduleRequestDTO.ListSearch condition,
            Pageable pageable
    ) {
        log.info("일정 리스트를 검색합니다: memberId={}, page={}, size={}", memberId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Schedule> pagedSchedules = scheduleRepository.searchSchedules(memberId, condition, pageable);
        return ScheduleConverter.toPagedListResultDTO(pagedSchedules);
    }

    // [Internal] Command 서비스에서 수정을 위해 엔티티를 조회할 때 사용
    public Schedule findByIdAndMemberId(Long scheduleId, Long memberId) {
        return scheduleRepository.findByIdAndMemberId(scheduleId, memberId)
                .orElseThrow(() -> new ScheduleException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
    }
}
