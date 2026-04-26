package com.smartschedule.smartschedule.domain.auth.exception.code.success;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthSuccessCode implements BaseSuccessCode {
    LOGIN_SUCCESS(
            HttpStatus.OK,
            "로그인에 성공했습니다.",
            "AUTH200_1"
    ),
    LOGOUT_SUCCESS(
            HttpStatus.OK,
            "로그아웃에 성공했습니다.",
            "AUTH200_2"
    ),
    TOKEN_REFRESH_SUCCESS(
            HttpStatus.OK,
            "토큰 재발급에 성공했습니다.",
            "AUTH200_3"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
