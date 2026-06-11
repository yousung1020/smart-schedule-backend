package com.smartschedule.smartschedule.domain.statistics.exception.code.error;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StatisticsErrorCode implements BaseErrorCode {
    INVALID_STATISTICS_PERIOD(
        HttpStatus.BAD_REQUEST,
        "조회 종료일은 시작일 이후여야 합니다.",
        "STATS400_1"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
