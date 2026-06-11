package com.smartschedule.smartschedule.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {
    BAD_REQUEST(
        HttpStatus.BAD_REQUEST,
        "잘못된 요청입니다.",
        "COMMON400_1"
    ),
    FORBIDDEN(
        HttpStatus.FORBIDDEN,
        "요청이 거부되었습니다.",
        "AUTH403_1"
    ),
    NOT_FOUND(
        HttpStatus.NOT_FOUND,
        "요청한 리소스를 찾을 수 없습니다.",
        "COMMON404_1"
    ),
    INTERNAL_SERVER_ERROR(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "예기치 않은 서버 에러가 발생했습니다.",
        "COMMON500_1"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
