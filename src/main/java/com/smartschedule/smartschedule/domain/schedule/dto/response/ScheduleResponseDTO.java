package com.smartschedule.smartschedule.domain.schedule.dto.response;

import com.smartschedule.smartschedule.domain.schedule.enums.Priority;
import com.smartschedule.smartschedule.domain.schedule.enums.ScheduleType;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class ScheduleResponseDTO {

    // 일정 단건 조회 상세 결과
    @Builder
    public record ScheduleResultDTO(
            Long id,
            String title,
            String content,
            LocalDateTime startTime,
            LocalDateTime endTime,
            Priority priority,
            boolean isCompleted,
            ScheduleType scheduleType,
            Long categoryId,
            String categoryName,
            String categoryColor,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    // 캘린더 뷰를 위한 리스트 응답 (페이징 X)
    @Builder
    public record CalendarListResultDTO(
            List<ScheduleResultDTO> schedules
    ) {}

    // 검색/리스트 뷰를 위한 페이징 응답 (페이징 메타데이터 포함)
    @Builder
    public record PagedListResultDTO(
            List<ScheduleResultDTO> content,
            int pageNumber,
            int pageSize,
            long totalElements,
            int totalPages,
            boolean isLast
    ) {}
}
