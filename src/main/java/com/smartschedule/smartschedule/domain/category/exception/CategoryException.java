package com.smartschedule.smartschedule.domain.category.exception;

import com.smartschedule.smartschedule.global.apiPayload.code.BaseErrorCode;
import com.smartschedule.smartschedule.global.apiPayload.exception.GeneralException;

public class CategoryException extends GeneralException {
    public CategoryException(BaseErrorCode errorCode) {
        super(errorCode);
    }
}
