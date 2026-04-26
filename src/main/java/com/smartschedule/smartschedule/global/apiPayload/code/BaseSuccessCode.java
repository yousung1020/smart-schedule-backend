package com.smartschedule.smartschedule.global.apiPayload.code;

import org.springframework.http.HttpStatus;

public interface BaseSuccessCode {
    HttpStatus getHttpStatus();

    String getMessage();

    String getCode();
}
