package com.smartschedule.smartschedule.domain.category.exception.code.error;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategoryErrorCode implements BaseErrorCode {
    CATEGORY_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "해당 카테고리를 찾을 수 없습니다.",
            "CATEGORY404_1"
    ),
    CATEGORY_ACCESS_DENIED(
            HttpStatus.FORBIDDEN,
            "해당 카테고리에 대한 접근 권한이 없습니다.",
            "CATEGORY403_1"
    ),
    ;

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
