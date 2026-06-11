package com.smartschedule.smartschedule.domain.schedule.exception.code.success;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ScheduleSuccessCode implements BaseSuccessCode {
    SCHEDULE_LIST_FETCH_SUCCESS(
        HttpStatus.OK,
        "일정 목록 조회에 성공했습니다.",
        "SCHEDULE200_1"
    ),
    SCHEDULE_FETCH_SUCCESS(
        HttpStatus.OK,
        "일정 상세 조회에 성공했습니다.",
        "SCHEDULE200_2"
    ),
    SCHEDULE_CREATE_SUCCESS(
        HttpStatus.CREATED,
        "일정 생성에 성공했습니다.",
        "SCHEDULE201_1"
    ),
    SCHEDULE_UPDATE_SUCCESS(
        HttpStatus.OK,
        "일정 수정에 성공했습니다.",
        "SCHEDULE200_3"
    ),
    SCHEDULE_COMPLETION_UPDATE_SUCCESS(
        HttpStatus.OK,
        "일정 완료 상태 변경에 성공했습니다.",
        "SCHEDULE200_4"
    ),
    SCHEDULE_DELETE_SUCCESS(
        HttpStatus.OK,
        "일정 삭제에 성공했습니다.",
        "SCHEDULE200_5"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
