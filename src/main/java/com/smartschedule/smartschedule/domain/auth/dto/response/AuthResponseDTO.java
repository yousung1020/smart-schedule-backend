package com.smartschedule.smartschedule.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

public class AuthResponseDTO {
    // Service -> Controller 내부 전달용
    @Builder
    public record TokenResultDTO(
        String accessToken,
        String refreshToken
    ) {}

    // Controller -> Client 응답용 (login, reissue)
    @Builder
    public record AccessTokenResultDTO(
        String accessToken,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        String refreshToken
    ) {
        // 웹 클라이언트 하위 호환 — refreshToken은 쿠키로 전달
        public AccessTokenResultDTO(String accessToken) {
            this(accessToken, null);
        }
    }
}
