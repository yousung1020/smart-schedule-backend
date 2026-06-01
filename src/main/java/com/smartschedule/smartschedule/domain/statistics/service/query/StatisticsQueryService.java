package com.smartschedule.smartschedule.domain.statistics.service.query;

import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;
import com.smartschedule.smartschedule.domain.statistics.exception.StatisticsException;
import com.smartschedule.smartschedule.domain.statistics.exception.code.error.StatisticsErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsQueryService {
    private final ScheduleRepository scheduleRepository;

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
                .collect(Collectors.toList()); // toList는 불변 리스트를 반환하므로, 가변 리스트를 반환
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
        
        List<StatisticsResponseDTO.WeeklyActivityResultDTO> rawList =
                scheduleRepository.getWeeklyActivity(memberId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        return rawList.stream()
                .map(item -> StatisticsResponseDTO.WeeklyActivityResultDTO.builder()
                        .weekLabel(formatWeekLabel(item.weekLabel()))
                        .count(item.count())
                        .build())
                .collect(Collectors.toList());
    }

    // 월간 활동 추이 통계 조회
    @Cacheable(
            value = "monthlyActivity",
            key = "#memberId + '_' + @cacheVersionService.getVersion('stats', #memberId) + '_' + #startDate + '_' + #endDate"
    )
    public List<StatisticsResponseDTO.MonthlyActivityResultDTO> getMonthlyActivity(
            Long memberId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.info("월간 활동 추이 통계를 조회합니다: memberId={}, range={} ~ {}", memberId, startDate, endDate);
        validatePeriod(startDate, endDate);
        
        List<StatisticsResponseDTO.MonthlyActivityResultDTO> rawList =
                scheduleRepository.getMonthlyActivity(memberId, startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        return rawList.stream()
                .map(item -> StatisticsResponseDTO.MonthlyActivityResultDTO.builder()
                        .monthLabel(formatMonthLabel(item.monthLabel()))
                        .count(item.count())
                        .build())
                .collect(Collectors.toList());
    }

    private String formatWeekLabel(String weekLabel) {
        if (weekLabel == null || !weekLabel.contains("-")) {
            return "";
        }

        String[] parts = weekLabel.split("-");

        if (parts.length != 2) {
            return weekLabel;
        }

        try {
            int year = Integer.parseInt(parts[0]);
            int week = Integer.parseInt(parts[1]);

            // MySQL WEEK(date, 0)과 동일한 일요일 시작 주차 규칙 정의
            WeekFields weekFields = WeekFields.SUNDAY_START;
            
            // 연도와 주차를 기준으로 해당 주의 첫 번째 요일(일요일) LocalDate 연산
            LocalDate startOfWeek = LocalDate.of(year, 1, 1)
                    .with(weekFields.weekOfYear(), week)
                    .with(weekFields.dayOfWeek(), 1); // 1 = Sunday (SUNDAY_START 기준)

            int month = startOfWeek.getMonthValue();
            int weekOfMonth = startOfWeek.get(weekFields.weekOfMonth());

            return String.format("%d월 %d주", month, weekOfMonth);
        } catch (NumberFormatException e) {
            log.warn("주간 활동 주차 라벨 파싱 실패 (포맷: YYYY-WW): {}", weekLabel, e);
            return weekLabel;
        }
    }

    private String formatMonthLabel(String monthLabel) {
        if (monthLabel == null || !monthLabel.contains("-")) {
            return "";
        }
        String[] parts = monthLabel.split("-");
        if (parts.length != 2) {
            return monthLabel;
        }
        try {
            int month = Integer.parseInt(parts[1]);
            return String.format("%d월", month);
        } catch (NumberFormatException e) {
            log.warn("월간 활동 월 라벨 파싱 실패 (포맷: YYYY-MM): {}", monthLabel, e);
            return monthLabel;
        }
    }

    // 조회 기간 유효성 검증
    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new StatisticsException(StatisticsErrorCode.INVALID_STATISTICS_PERIOD);
        }
    }
}
