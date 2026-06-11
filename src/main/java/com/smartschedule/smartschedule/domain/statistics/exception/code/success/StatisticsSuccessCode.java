package com.smartschedule.smartschedule.domain.statistics.exception.code.success;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum StatisticsSuccessCode implements BaseSuccessCode {
    COMPLETION_RATE_FETCH_SUCCESS(
        HttpStatus.OK,
        "일정 완료율 조회에 성공했습니다.",
        "STATS200_1"
    ),
    CATEGORY_DIST_FETCH_SUCCESS(
        HttpStatus.OK,
        "카테고리별 점유율 조회에 성공했습니다.",
        "STATS200_2"
    ),
    WEEKLY_ACTIVITY_FETCH_SUCCESS(
        HttpStatus.OK,
        "주간 활동 추이 조회에 성공했습니다.",
        "STATS200_3"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
