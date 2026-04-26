package com.smartschedule.smartschedule.domain.auth.exception;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import com.smartschedule.smartschedule.global.apiPayload.exception.GeneralException;

public class AuthException extends GeneralException {
    public AuthException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
