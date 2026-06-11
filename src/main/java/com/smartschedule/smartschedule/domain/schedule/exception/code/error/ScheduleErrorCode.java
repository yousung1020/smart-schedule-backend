package com.smartschedule.smartschedule.domain.schedule.exception.code.error;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseErrorCode {
    SCHEDULE_NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "해당 일정을 찾을 수 없습니다.",
        "SCHEDULE404_1"
    ),
    SCHEDULE_ACCESS_DENIED(
        HttpStatus.FORBIDDEN,
        "해당 일정에 대한 접근 권한이 없습니다.",
        "SCHEDULE403_1"
    ),
    INVALID_SCHEDULE_DATE(
        HttpStatus.BAD_REQUEST,
        "종료 시간은 시작 시간 이후여야 합니다.",
        "SCHEDULE400_1"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
