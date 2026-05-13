package com.smartschedule.smartschedule.domain.statistics.service.query;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;
import com.smartschedule.smartschedule.domain.statistics.exception.StatisticsException;
import com.smartschedule.smartschedule.domain.statistics.exception.code.error.StatisticsErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatisticsQueryServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private StatisticsQueryService statisticsQueryService;

    @Test
    @DisplayName("카테고리별 점유율 계산 로직 검증")
    void getCategoryDistribution_CalculatesShareCorrectly() {
        // given
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        // Repository로부터 가져올 가공되지 않은 기초 데이터 (share는 0.0인 상태)
        List<StatisticsResponseDTO.CategoryDistributionResultDTO> rawStats = List.of(
                StatisticsResponseDTO.CategoryDistributionResultDTO.builder()
                        .categoryId(1L).categoryName("Work").count(7L).share(0.0).build(),
                StatisticsResponseDTO.CategoryDistributionResultDTO.builder()
                        .categoryId(2L).categoryName("Study").count(3L).share(0.0).build()
        );

        when(scheduleRepository.getCategoryDistribution(eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(rawStats);

        // when
        List<StatisticsResponseDTO.CategoryDistributionResultDTO> result =
                statisticsQueryService.getCategoryDistribution(memberId, startDate, endDate);

        // then
        assertNotNull(result);
        assertEquals(2, result.size());

        // Work: 7 / (7+3) * 100 = 70.0
        assertEquals(70.0, result.get(0).share());
        assertEquals("Work", result.get(0).categoryName());

        // Study: 3 / (7+3) * 100 = 30.0
        assertEquals(30.0, result.get(1).share());
        assertEquals("Study", result.get(1).categoryName());
    }

    @Test
    @DisplayName("일정이 없을 때 점유율 0.0 반환 검증 (Zero Division 방지)")
    void getCategoryDistribution_EmptySchedules_ReturnsZeroShare() {
        // given
        Long memberId = 1L;
        when(scheduleRepository.getCategoryDistribution(eq(memberId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());

        // when
        List<StatisticsResponseDTO.CategoryDistributionResultDTO> result =
                statisticsQueryService.getCategoryDistribution(memberId, LocalDate.now(), LocalDate.now());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("시작일이 종료일보다 늦을 때 예외 발생 검증")
    void validatePeriod_ThrowsException_WhenStartAfterEnd() {
        // given
        Long memberId = 1L;
        LocalDate startDate = LocalDate.of(2024, 1, 31);
        LocalDate endDate = LocalDate.of(2024, 1, 1);

        // when & then
        StatisticsException exception = assertThrows(
                StatisticsException.class,
                () -> statisticsQueryService.getCategoryDistribution(memberId, startDate, endDate)
        );

        assertEquals(StatisticsErrorCode.INVALID_STATISTICS_PERIOD, exception.getCode());
    }
}
