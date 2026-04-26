package com.smartschedule.smartschedule.domain.member.exception;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import com.smartschedule.smartschedule.global.apiPayload.exception.GeneralException;

public class MemberException extends GeneralException {
    public MemberException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
