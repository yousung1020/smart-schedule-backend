package com.smartschedule.smartschedule.domain.statistics.controller;

import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;
import com.smartschedule.smartschedule.domain.statistics.exception.code.success.StatisticsSuccessCode;
import com.smartschedule.smartschedule.domain.statistics.service.query.StatisticsQueryService;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/stats")
public class StatisticsController {

    private final StatisticsQueryService statisticsQueryService;

    // 일정 완료율 조회 (기간 검색)
    @GetMapping("/completion-rate")
    public ApiResponse<StatisticsResponseDTO.CompletionRateResultDTO> getCompletionRate(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        var result = statisticsQueryService.getCompletionRate(userDetails.getMemberId(), startDate, endDate);
        return ApiResponse.onSuccess(StatisticsSuccessCode.COMPLETION_RATE_FETCH_SUCCESS, result);
    }

    // 카테고리별 일정 점유율 조회 (기간 검색)
    @GetMapping("/category-distribution")
    public ApiResponse<List<StatisticsResponseDTO.CategoryDistributionResultDTO>> getCategoryDistribution(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        var result = statisticsQueryService.getCategoryDistribution(userDetails.getMemberId(), startDate, endDate);
        return ApiResponse.onSuccess(StatisticsSuccessCode.CATEGORY_DIST_FETCH_SUCCESS, result);
    }

    // 최근 주차별 활동량 통계 조회 (기간 검색)
    @GetMapping("/weekly-activity")
    public ApiResponse<List<StatisticsResponseDTO.WeeklyActivityResultDTO>> getWeeklyActivity(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        var result = statisticsQueryService.getWeeklyActivity(userDetails.getMemberId(), startDate, endDate);
        return ApiResponse.onSuccess(StatisticsSuccessCode.WEEKLY_ACTIVITY_FETCH_SUCCESS, result);
    }
}
