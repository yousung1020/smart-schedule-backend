package com.smartschedule.smartschedule.domain.schedule.repository;

import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;

public interface ScheduleRepositoryCustom {
    // 페이징 없이 조건에 맞는 일정 리스트 반환(캘린더 뷰)
    List<Schedule> findCalendarSchedules(Long memberId, ScheduleRequestDTO.CalendarSearch condition);

    // 다중 필터 및 페이징을 지원하는 검색 반환(리스트 뷰)
    Page<Schedule> searchSchedules(Long memberId, ScheduleRequestDTO.ListSearch condition, Pageable pageable);

    // 일정 완료율 조회 (특정 기간 내 DONE 상태 비율 산출)
    StatisticsResponseDTO.CompletionRateResultDTO getCompletionRate(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    // 카테고리별 일정 분포 조회 (특정 기간 내 카테고리별 개수 집계)
    List<StatisticsResponseDTO.CategoryDistributionResultDTO> getCategoryDistribution(Long memberId, LocalDateTime startDate, LocalDateTime endDate);

    // 주간 활동 추이 조회 (특정 기간 내 주차별 생성된 일정 수 집계)
    List<StatisticsResponseDTO.WeeklyActivityResultDTO> getWeeklyActivity(Long memberId, LocalDateTime startDate, LocalDateTime endDate);
}
