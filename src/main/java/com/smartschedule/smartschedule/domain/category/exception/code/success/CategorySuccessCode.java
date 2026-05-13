package com.smartschedule.smartschedule.domain.category.exception.code.success;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CategorySuccessCode implements BaseSuccessCode {
    CATEGORY_LIST_FETCH_SUCCESS(
            HttpStatus.OK,
            "카테고리 목록 조회에 성공했습니다.",
            "CATEGORY200_1"
    ),
    CATEGORY_CREATE_SUCCESS(
            HttpStatus.CREATED,
            "카테고리 생성에 성공했습니다.",
            "CATEGORY201_1"
    ),
    CATEGORY_UPDATE_SUCCESS(
            HttpStatus.OK,
            "카테고리 수정에 성공했습니다.",
            "CATEGORY200_2"
    ),
    CATEGORY_DELETE_SUCCESS(
            HttpStatus.OK,
            "카테고리 삭제에 성공했습니다.",
            "CATEGORY200_3"
    );

    private final HttpStatus httpStatus;
    private final String message;
    private final String code;
}
