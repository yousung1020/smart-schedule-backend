package com.smartschedule.smartschedule.domain.schedule.controller;

import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.dto.response.ScheduleResponseDTO;
import com.smartschedule.smartschedule.domain.schedule.exception.code.success.ScheduleSuccessCode;
import com.smartschedule.smartschedule.domain.schedule.service.command.ScheduleCommandService;
import com.smartschedule.smartschedule.domain.schedule.service.query.ScheduleQueryService;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleCommandService scheduleCommandService;
    private final ScheduleQueryService scheduleQueryService;

    // 캘린더 뷰 (페이징 X)
    @GetMapping("/calendar")
    public ApiResponse<ScheduleResponseDTO.CalendarListResultDTO> getCalendarSchedules(
            @ModelAttribute @Valid ScheduleRequestDTO.CalendarSearch condition,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleQueryService.getCalendarSchedules(userDetails.getMemberId(), condition);
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_LIST_FETCH_SUCCESS, result);
    }

    // 리스트 뷰 (페이징 O, 기본 사이즈 10)
    @GetMapping
    public ApiResponse<ScheduleResponseDTO.PagedListResultDTO> searchSchedules(
            @ModelAttribute ScheduleRequestDTO.ListSearch condition,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleQueryService.searchSchedules(userDetails.getMemberId(), condition, pageable);
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_LIST_FETCH_SUCCESS, result);
    }

    // 단건 상세 조회
    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.ScheduleResultDTO> getScheduleDetail(
            @PathVariable("scheduleId") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleQueryService.getScheduleDetail(scheduleId, userDetails.getMemberId());
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_FETCH_SUCCESS, result);
    }

    // 일정 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ScheduleResponseDTO.ScheduleResultDTO> createSchedule(
            @RequestBody @Valid ScheduleRequestDTO.ScheduleCreateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleCommandService.createSchedule(request, userDetails.getMemberId());
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_CREATE_SUCCESS, result);
    }

    // 일정 수정
    @PatchMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponseDTO.ScheduleResultDTO> updateSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody @Valid ScheduleRequestDTO.ScheduleUpdateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleCommandService.updateSchedule(scheduleId, request, userDetails.getMemberId());
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_UPDATE_SUCCESS, result);
    }

    // 완료 상태 변경
    @PatchMapping("/{scheduleId}/completion")
    public ApiResponse<ScheduleResponseDTO.ScheduleResultDTO> updateScheduleCompletion(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestBody @Valid ScheduleRequestDTO.ScheduleCompletionUpdateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        var result = scheduleCommandService.updateScheduleCompletion(scheduleId, request.isCompleted(), userDetails.getMemberId());
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_COMPLETION_UPDATE_SUCCESS, result);
    }

    // 일정 삭제
    @DeleteMapping("/{scheduleId}")
    public ApiResponse<Void> deleteSchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        scheduleCommandService.deleteSchedule(scheduleId, userDetails.getMemberId());
        return ApiResponse.onSuccess(ScheduleSuccessCode.SCHEDULE_DELETE_SUCCESS, null);
    }
}
