package com.smartschedule.smartschedule.domain.statistics.dto.response;

import lombok.Builder;

public class StatisticsResponseDTO {

    @Builder
    public record CompletionRateResultDTO(
            Double completionRate,
            Long totalCount,
            Long doneCount
    ) {}

    @Builder
    public record CategoryDistributionResultDTO(
            Long categoryId,
            String categoryName,
            Long count,
            Double share
    ) {}

    @Builder
    public record WeeklyActivityResultDTO(
            String weekLabel,
            Long count
    ) {}
}
