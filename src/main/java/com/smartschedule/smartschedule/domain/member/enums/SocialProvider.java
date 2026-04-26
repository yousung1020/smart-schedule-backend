package com.smartschedule.smartschedule.domain.member.enums;

import com.smartschedule.smartschedule.domain.auth.exception.AuthException;
import com.smartschedule.smartschedule.domain.auth.exception.code.error.AuthErrorCode;
import java.util.Locale;

public enum SocialProvider {
    KAKAO, NAVER, GOOGLE;

    public static SocialProvider fromString(String providerName) {
        if (providerName == null) {
            throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }

        try {
            return valueOf(providerName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        }
    }
}

