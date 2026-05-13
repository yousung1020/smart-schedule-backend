package com.smartschedule.smartschedule.domain.statistics.service.query;

import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;
import com.smartschedule.smartschedule.domain.statistics.exception.StatisticsException;
import com.smartschedule.smartschedule.domain.statistics.exception.code.error.StatisticsErrorCode;
import com.smartschedule.smartschedule.global.cache.CacheVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsQueryService {

    private final ScheduleRepository scheduleRepository;
    private final CacheVersionService cacheVersionService;

    // 일정 완료율 통계 조회
    @Cacheable(
            value = "completionRate",
            key = "#memberId + '_' + @cacheVersionService.getVersion('stats', #memberId) + '_' + #startDate + '_' + #endDate"
    )
    public StatisticsResponseDTO.CompletionRateResultDTO getCompletionRate(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("일정 완료율 통계를 조회합니다: memberId={}, range={} ~ {}", memberId, startDate, endDate);
        validatePeriod(startDate, endDate);
        
        return scheduleRepository.getCompletionRate(memberId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    // 카테고리별 일정 분포 통계 조회
    @Cacheable(
            value = "categoryDistribution",
            key = "#memberId + '_' + @cacheVersionService.getVersion('stats', #memberId) + '_' + #startDate + '_' + #endDate"
    )
    public List<StatisticsResponseDTO.CategoryDistributionResultDTO> getCategoryDistribution(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("카테고리별 일정 분포 통계를 조회합니다: memberId={}, range={} ~ {}", memberId, startDate, endDate);
        validatePeriod(startDate, endDate);

        // 카테고리별 일정 개수 조회
        List<StatisticsResponseDTO.CategoryDistributionResultDTO> rawStats =
                scheduleRepository.getCategoryDistribution(memberId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        // 전체 일정 개수 합산
        long totalCount = rawStats.stream()
                .mapToLong(StatisticsResponseDTO.CategoryDistributionResultDTO::count)
                .sum();

        // 점유율 계산 및 빌더를 통한 DTO 조립
        return rawStats.stream()
                .map(s -> {
                    // 점유율 계산
                    double calculatedShare = (totalCount == 0) ? 0.0 : (double) s.count() / totalCount * 100.0;

                    return StatisticsResponseDTO.CategoryDistributionResultDTO.builder()
                            .categoryId(s.categoryId())
                            .categoryName(s.categoryName())
                            .count(s.count())
                            .share(calculatedShare)
                            .build();
                })
                .toList();
    }

    // 주간 활동 추이 통계 조회
    @Cacheable(
            value = "weeklyActivity",
            key = "#memberId + '_' + @cacheVersionService.getVersion('stats', #memberId) + '_' + #startDate + '_' + #endDate"
    )
    public List<StatisticsResponseDTO.WeeklyActivityResultDTO> getWeeklyActivity(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("주간 활동 추이 통계를 조회합니다: memberId={}, range={} ~ {}", memberId, startDate, endDate);
        validatePeriod(startDate, endDate);
        
        return scheduleRepository.getWeeklyActivity(memberId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
    }

    // 조회 기간 유효성 검증
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new StatisticsException(StatisticsErrorCode.INVALID_STATISTICS_PERIOD);
        }
    }
}
