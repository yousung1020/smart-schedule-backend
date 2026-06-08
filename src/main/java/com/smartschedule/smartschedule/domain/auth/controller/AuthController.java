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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtProperties jwtProperties;

    @Value("${COOKIE_SECURE:false}")
    private boolean cookieSecure;

    @Value("${COOKIE_SAME_SITE:Lax}")
    private String cookieSameSite;

    // 일반 회원가입
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<String> signup(@RequestBody @Valid AuthRequestDTO.SignupDTO request, HttpServletResponse response) {
        authService.signup(request);
        return ApiResponse.onSuccess(
            AuthSuccessCode.SIGNUP_SUCCESS,
            "회원가입에 성공하였습니다."
        );
    }

    // 일반 로그인
    @PostMapping("/login")
    public ApiResponse<AuthResponseDTO.AccessTokenResultDTO> login(@RequestBody @Valid AuthRequestDTO.LoginDTO request, HttpServletResponse response) {
        AuthResponseDTO.TokenResultDTO tokens = authService.login(request);
        return handleTokenResponse(tokens, response, AuthSuccessCode.LOGIN_SUCCESS);
    }

    // 소셜 로그인
    @PostMapping("/login/{provider}")
    public ApiResponse<AuthResponseDTO.AccessTokenResultDTO> socialLogin(@PathVariable("provider") String provider, @RequestBody @Valid AuthRequestDTO.SocialLoginDTO request, HttpServletResponse response) {
        AuthResponseDTO.TokenResultDTO tokens = authService.socialLogin(provider, request);
        return handleTokenResponse(tokens, response, AuthSuccessCode.LOGIN_SUCCESS);
    }

    // 로그아웃
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String accessToken, HttpServletResponse response) {
        authService.logout(accessToken);
        setRefreshTokenCookie(response, "", 0);
        return ApiResponse.onSuccess(AuthSuccessCode.LOGOUT_SUCCESS, null);
    }

    // 토큰 재발급
    @PostMapping("/reissue")
    public ApiResponse<AuthResponseDTO.AccessTokenResultDTO> reissueToken(@CookieValue(value = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
        AuthResponseDTO.TokenResultDTO tokens = authService.reissueToken(refreshToken);
        return handleTokenResponse(tokens, response, AuthSuccessCode.TOKEN_REFRESH_SUCCESS);
    }

    // 비밀번호 재설정 요청
    @PostMapping("/password/reset-request")
    public ApiResponse<Void> requestPasswordReset(@RequestBody @Valid AuthRequestDTO.PasswordResetRequestDTO request) {
        authService.requestPasswordReset(request);
        return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_RESET_REQUEST_SUCCESS, null);
    }

    // 비밀번호 재설정 실행
    @PostMapping("/password/reset")
    public ApiResponse<Void> resetPassword(@RequestBody @Valid AuthRequestDTO.PasswordResetDTO request) {
        authService.resetPassword(request);
        return ApiResponse.onSuccess(AuthSuccessCode.PASSWORD_RESET_SUCCESS, null);
    }

    // 토큰 응답 공통 처리 헬퍼
    private ApiResponse<AuthResponseDTO.AccessTokenResultDTO> handleTokenResponse(AuthResponseDTO.TokenResultDTO tokens, HttpServletResponse response, AuthSuccessCode successCode) {
        int maxAge = (int) (jwtProperties.getRefreshToken().getExpirationTime() / 1000);
        setRefreshTokenCookie(response, tokens.getRefreshToken(), maxAge);
        return ApiResponse.onSuccess(successCode, new AuthResponseDTO.AccessTokenResultDTO(tokens.getAccessToken()));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
