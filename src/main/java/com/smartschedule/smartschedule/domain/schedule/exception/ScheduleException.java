package com.smartschedule.smartschedule.domain.schedule.exception;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import com.smartschedule.smartschedule.global.apiPayload.exception.GeneralException;

public class ScheduleException extends GeneralException {
    public ScheduleException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
