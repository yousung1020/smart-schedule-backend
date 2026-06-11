package com.smartschedule.smartschedule.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

public class AuthRequestDTO {
    @Builder
    public record SignupDTO(
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        String password,

        @NotBlank(message = "닉네임은 필수 입력값입니다.")
        String nickname
    ) {}

    @Builder
    public record LoginDTO(
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력값입니다.")
        String password
    ) {}

    @Builder
    public record SocialLoginDTO(
        @NotBlank(message = "인가 코드는 필수 입력값입니다.")
        String authorizationCode
    ) {}

    @Builder
    public record ReissueDTO(
        @NotBlank(message = "리프레시 토큰은 필수 입력값입니다.")
        String refreshToken
    ) {}

    @Builder
    public record PasswordResetRequestDTO(
        @NotBlank(message = "이메일은 필수 입력값입니다.")
        @Email(message = "이메일 형식이 올바르지 않습니다.")
        String email
    ) {}

    @Builder
    public record PasswordResetDTO(
        @NotBlank(message = "토큰은 필수 입력값입니다.")
        String token,

        @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
        String newPassword
    ) {}
}
