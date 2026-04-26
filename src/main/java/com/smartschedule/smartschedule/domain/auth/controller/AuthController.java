package com.smartschedule.smartschedule.domain.auth.controller;

import com.smartschedule.smartschedule.domain.auth.dto.request.AuthRequestDTO;
import com.smartschedule.smartschedule.domain.auth.dto.response.AuthResponseDTO;
import com.smartschedule.smartschedule.domain.auth.exception.AuthException;
import com.smartschedule.smartschedule.domain.auth.exception.code.error.AuthErrorCode;
import com.smartschedule.smartschedule.domain.auth.exception.code.success.AuthSuccessCode;
import com.smartschedule.smartschedule.domain.auth.service.AuthService;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.config.JwtProperties;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @PostMapping("/login/{provider}")
    public ApiResponse<AuthResponseDTO.AccessTokenResultDTO> socialLogin(
            @PathVariable("provider") String provider,
            @RequestBody @Valid AuthRequestDTO.SocialLoginDTO request,
            HttpServletResponse response
    ) {
        AuthResponseDTO.TokenResultDTO tokens = authService.socialLogin(provider, request);

        // 브라우저에 Refresh Token을 쿠키로 굽기 (밀리초를 초 단위로 변환)
        int maxAge = (int) (jwtProperties.getRefreshToken().getExpirationTime() / 1000);
        setRefreshTokenCookie(response, tokens.getRefreshToken(), maxAge);

        AuthResponseDTO.AccessTokenResultDTO accessOnlyResponse = new AuthResponseDTO.AccessTokenResultDTO(
                tokens.getAccessToken()
        );

        return ApiResponse.onSuccess(AuthSuccessCode.LOGIN_SUCCESS, accessOnlyResponse);
    }

    // Access Token을 Authorization 헤더에서 추출하여 블랙리스트 처리 및 Redis의 Refresh Token을 삭제합니다.
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader("Authorization") String accessToken,
            HttpServletResponse response
    ) {
        authService.logout(accessToken);

        // 브라우저의 Refresh Token 쿠키 삭제 (Max-Age = 0)
        setRefreshTokenCookie(response, "", 0);

        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ApiResponse<AuthResponseDTO.AccessTokenResultDTO> reissueToken(
            @CookieValue(value = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        AuthResponseDTO.TokenResultDTO tokens = authService.reissueToken(refreshToken);

        // 새로 발급된 Refresh Token을 다시 쿠키에 굽기
        int maxAge = (int) (jwtProperties.getRefreshToken().getExpirationTime() / 1000);
        setRefreshTokenCookie(response, tokens.getRefreshToken(), maxAge);

        AuthResponseDTO.AccessTokenResultDTO accessOnlyResponse = new AuthResponseDTO.AccessTokenResultDTO(
                tokens.getAccessToken());

        return ApiResponse.onSuccess(
                AuthSuccessCode.TOKEN_REFRESH_SUCCESS,
                accessOnlyResponse
        );
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            int maxAge
    ) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(false) // 로컬 개발을 위해 false 유지. CI/CD 환경 구성 후 true로 변경 필수
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}


