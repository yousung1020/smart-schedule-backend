package com.smartschedule.smartschedule.domain.schedule.converter;

import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.dto.response.ScheduleResponseDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import org.springframework.data.domain.Page;

import java.util.List;

public class ScheduleConverter {

    public static Schedule toEntity(
            ScheduleRequestDTO.ScheduleCreateDTO request,
            Member member,
            Category category
    ) {
        return Schedule.builder()
                .title(request.title())
                .content(request.content())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .priority(request.priority())
                .isCompleted(false) // 신규 생성 시 기본 미완료
                .scheduleType(request.scheduleType())
                .member(member)
                .category(category)
                .build();
    }

    public static ScheduleResponseDTO.ScheduleResultDTO toResultDTO(Schedule schedule) {
        String categoryName = schedule.getCategory() != null ? schedule.getCategory().getName() : null;
        String categoryColor = schedule.getCategory() != null ? schedule.getCategory().getColor() : null;
        Long categoryId = schedule.getCategory() != null ? schedule.getCategory().getId() : null;

        return ScheduleResponseDTO.ScheduleResultDTO.builder()
                .id(schedule.getId())
                .title(schedule.getTitle())
                .content(schedule.getContent())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .priority(schedule.getPriority())
                .isCompleted(schedule.isCompleted())
                .scheduleType(schedule.getScheduleType())
                .categoryId(categoryId)
                .categoryName(categoryName)
                .categoryColor(categoryColor)
                .createdAt(schedule.getCreatedAt())
                .updatedAt(schedule.getUpdatedAt())
                .build();
    }

    public static ScheduleResponseDTO.CalendarListResultDTO toCalendarListResultDTO(List<Schedule> schedules) {
        List<ScheduleResponseDTO.ScheduleResultDTO> resultList = schedules.stream()
                .map(ScheduleConverter::toResultDTO)
                .toList();

        return ScheduleResponseDTO.CalendarListResultDTO.builder()
                .schedules(resultList)
                .build();
    }

    public static ScheduleResponseDTO.PagedListResultDTO toPagedListResultDTO(Page<Schedule> schedulePage) {
        List<ScheduleResponseDTO.ScheduleResultDTO> resultList = schedulePage.getContent().stream()
                .map(ScheduleConverter::toResultDTO)
                .toList();

        return ScheduleResponseDTO.PagedListResultDTO.builder()
                .content(resultList)
                .pageNumber(schedulePage.getNumber())
                .pageSize(schedulePage.getSize())
                .totalElements(schedulePage.getTotalElements())
                .totalPages(schedulePage.getTotalPages())
                .isLast(schedulePage.isLast())
                .build();
    }
}
