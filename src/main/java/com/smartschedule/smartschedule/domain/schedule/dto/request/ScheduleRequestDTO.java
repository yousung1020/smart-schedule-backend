package com.smartschedule.smartschedule.domain.schedule.dto.request;

import com.smartschedule.smartschedule.domain.schedule.enums.Priority;
import com.smartschedule.smartschedule.domain.schedule.enums.ScheduleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

public class ScheduleRequestDTO {
    @Builder
    public record ScheduleCreateDTO(
            @NotBlank(message = "일정 제목은 필수입니다.")
            @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
            String title,

            String content,

            @NotNull(message = "시작 시간은 필수입니다.")
            LocalDateTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            LocalDateTime endTime,

            @NotNull(message = "중요도는 필수입니다.")
            Priority priority,

            Long categoryId,
            
            @NotNull(message = "일정 구분(시작/마감)은 필수입니다.")
            ScheduleType scheduleType,

            List<Integer> notifyBeforeMinutes
    ) {}

    @Builder
    public record ScheduleUpdateDTO(
            @NotBlank(message = "일정 제목은 필수입니다.")
            @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
            String title,

            String content,

            @NotNull(message = "시작 시간은 필수입니다.")
            LocalDateTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            LocalDateTime endTime,

            @NotNull(message = "중요도는 필수입니다.")
            Priority priority,

            Long categoryId,
            
            @NotNull(message = "일정 구분(시작/마감)은 필수입니다.")
            ScheduleType scheduleType,

            List<Integer> notifyBeforeMinutes
    ) {}

    // 캘린더 뷰 조회를 위한 조건 (GET 파라미터 매핑을 위한 날짜 포맷 지정)
    @Builder
    public record CalendarSearch(
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startDate,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            
            Long categoryId
    ) {}

    // 검색/리스트 뷰 조회를 위한 조건
    @Builder
    public record ListSearch(
            String keyword,
            Priority priority,
            Boolean isCompleted,
            Long categoryId
    ) {}

    // 상태 변경 전용 DTO
    @Builder
    public record ScheduleCompletionUpdateDTO(
            @NotNull(message = "변경할 상태 값은 필수입니다.")
            Boolean isCompleted
    ) {}
}
