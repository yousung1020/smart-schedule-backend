package com.smartschedule.smartschedule.domain.statistics.exception;

import com.smartschedule.smartschedule.domain.statistics.exception.code.error.StatisticsErrorCode;
import com.smartschedule.smartschedule.global.apiPayload.exception.GeneralException;

public class StatisticsException extends GeneralException {
    public StatisticsException(StatisticsErrorCode code) {
        super(code);
    }
}
