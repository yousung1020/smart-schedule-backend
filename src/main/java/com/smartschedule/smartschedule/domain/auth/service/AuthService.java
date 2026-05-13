package com.smartschedule.smartschedule.domain.auth.service;

import com.smartschedule.smartschedule.domain.auth.client.KakaoOAuthClient;
import com.smartschedule.smartschedule.domain.auth.client.OAuth2UserInfo;
import com.smartschedule.smartschedule.domain.auth.dto.request.AuthRequestDTO;
import com.smartschedule.smartschedule.domain.auth.dto.response.AuthResponseDTO;
import com.smartschedule.smartschedule.domain.auth.dto.response.KakaoTokenResponseDTO;
import com.smartschedule.smartschedule.domain.auth.exception.AuthException;
import com.smartschedule.smartschedule.domain.auth.exception.code.error.AuthErrorCode;
import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.service.command.MemberCommandService;
import com.smartschedule.smartschedule.domain.member.service.query.MemberQueryService;
import com.smartschedule.smartschedule.global.util.JwtUtil;
import com.smartschedule.smartschedule.global.util.MailService;
import com.smartschedule.smartschedule.global.util.RedisUtil;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String REDIS_RT_PREFIX = "RT:";
    private static final String REDIS_PW_RESET_PREFIX = "PW_RESET:";
    private static final String BEARER_PREFIX_LOWER = "bearer ";

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;
    private final KakaoOAuthClient kakaoOAuthClient;

    // 비밀번호 재설정 요청
    public void requestPasswordReset(AuthRequestDTO.PasswordResetRequestDTO request) {
        Member member = memberQueryService.findByEmail(request.getEmail());
        
        // 소셜 로그인 사용자인지 확인
        if (member.getSocialProvider() != null) {
            throw new MemberException(MemberErrorCode.CANNOT_RESET_SOCIAL_PASSWORD);
        }
        
        String resetToken = UUID.randomUUID().toString();
        redisUtil.set(REDIS_PW_RESET_PREFIX + resetToken, member.getEmail(), Duration.ofMinutes(15));
        
        String resetLink = "http://localhost:5173/reset-password?token=" + resetToken;
        mailService.sendPasswordResetMail(member.getEmail(), resetLink);
        
        log.info("비밀번호를 초기화하는 메일을 성공적으로 보냄: {}", member.getEmail());
    }

    // 비밀번호 재설정 실행
    public void resetPassword(AuthRequestDTO.PasswordResetDTO request) {
        String email = (String) redisUtil.getAndDelete(REDIS_PW_RESET_PREFIX + request.getToken());
        
        if (email == null) {
            throw new AuthException(AuthErrorCode.RESET_TOKEN_INVALID);
        }
        
        Member member = memberQueryService.findByEmail(email);
        memberCommandService.updatePassword(member, passwordEncoder.encode(request.getNewPassword()));
        
        log.info("패스워드가 성공적으로 초기화됨: {}", email);
    }

    // 일반 회원가입
    public AuthResponseDTO.TokenResultDTO signup(AuthRequestDTO.SignupDTO request) {
        log.info("일반 회원가입을 시도합니다: email={}", request.getEmail());
        MemberResponseDTO.MemberResultDTO memberDTO = memberCommandService.createMember(request);
        log.info("일반 회원가입이 완료되었습니다: memberId={}", memberDTO.id());
        return generateAndSaveTokens(memberDTO.id(), memberDTO.role());
    }

    // 일반 로그인
    public AuthResponseDTO.TokenResultDTO login(AuthRequestDTO.LoginDTO request) {
        log.info("일반 로그인을 시도합니다: email={}", request.getEmail());
        Member member = memberQueryService.findByEmail(request.getEmail());

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            log.warn("로그인 실패: 비밀번호 불일치 - email={}", request.getEmail());
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        log.info("일반 로그인이 완료되었습니다: memberId={}", member.getId());
        return generateAndSaveTokens(member.getId(), member.getRole());
    }

    // 소셜 로그인
    public AuthResponseDTO.TokenResultDTO socialLogin(String provider, AuthRequestDTO.SocialLoginDTO request) {
        log.info("소셜 로그인을 시도합니다: provider={}", provider);
        SocialProvider socialProvider = SocialProvider.fromString(provider);
        OAuth2UserInfo userInfo = fetchUserInfo(socialProvider, request.getAuthorizationCode());

        String socialId = userInfo.getSocialId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        // Query와 Command 모두 MemberResultDTO를 반환하므로 타입이 일치함
        MemberResponseDTO.MemberResultDTO memberDTO = memberQueryService.findBySocialIdAndProvider(socialId, socialProvider)
                .orElseGet(() -> memberCommandService.createSocialMember(email, nickname, socialId, socialProvider));

        log.info("소셜 로그인이 완료되었습니다: provider={}, memberId={}", provider, memberDTO.id());
        return generateAndSaveTokens(memberDTO.id(), memberDTO.role());
    }

    // 로그아웃
    public void logout(String accessToken) {
        String resolvedToken = resolveToken(accessToken);
        Long memberId;

        try {
            String subject = jwtUtil.getClaimsForLogout(resolvedToken).getSubject();
            if (subject == null || subject.trim().isEmpty()) {
                throw new IllegalArgumentException("Subject is empty");
            }
            memberId = Long.parseLong(subject);
        } catch (Exception e) {
            log.warn("로그아웃 실패: 유효하지 않은 토큰. error={}", e.getMessage());
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        String redisKey = REDIS_RT_PREFIX + memberId;
        if (redisUtil.hasKey(redisKey)) {
            redisUtil.delete(redisKey);
        }

        Long expirationMillis = jwtUtil.getExpirationTime(resolvedToken);
        redisUtil.setBlackList(resolvedToken, expirationMillis);

        log.info("사용자 로그아웃 완료: memberId={}", memberId);
    }

    // 토큰 재발급
    public AuthResponseDTO.TokenResultDTO reissueToken(String refreshToken) {
        String resolvedToken = resolveToken(refreshToken);

        if (!jwtUtil.validateToken(resolvedToken)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        String category = jwtUtil.getClaims(resolvedToken).get("category", String.class);
        if (!"refresh".equals(category)) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        Long memberId = extractMemberId(resolvedToken);
        String redisKey = REDIS_RT_PREFIX + memberId;

        String storedRefreshToken = (String) redisUtil.getAndDelete(redisKey);
        if (storedRefreshToken == null || !storedRefreshToken.equals(resolvedToken)) {
            log.warn("토큰 재발급 실패: 탈취 의심 / memberId={}", memberId);
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }

        Member member = memberQueryService.findById(memberId);

        return generateAndSaveTokens(member.getId(), member.getRole());
    }

    private OAuth2UserInfo fetchUserInfo(SocialProvider provider, String code) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponseDTO tokenResponse = kakaoOAuthClient.fetchKakaoAccessToken(code);
                yield kakaoOAuthClient.fetchKakaoUserInfo(tokenResponse.accessToken());
            }
            default -> throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        };
    }

    private AuthResponseDTO.TokenResultDTO generateAndSaveTokens(Long memberId, Role role) {
        String accessToken = jwtUtil.createAccessToken(memberId, role);
        String refreshToken = jwtUtil.createRefreshToken(memberId);

        Long expirationMillis = jwtUtil.getExpirationTime(refreshToken);
        redisUtil.set(REDIS_RT_PREFIX + memberId, refreshToken, Duration.ofMillis(expirationMillis));

        return new AuthResponseDTO.TokenResultDTO(accessToken, refreshToken);
    }

    private String resolveToken(String token) {
        if (token != null) {
            String trimmedToken = token.trim();
            if (trimmedToken.toLowerCase().startsWith(BEARER_PREFIX_LOWER)) {
                return trimmedToken.substring(7).trim();
            }
        }
        return token;
    }

    private Long extractMemberId(String token) {
        String subject = jwtUtil.getClaims(token).getSubject();
        if (subject == null || subject.trim().isEmpty()) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
        try {
            return Long.parseLong(subject);
        } catch (NumberFormatException e) {
            throw new AuthException(AuthErrorCode.TOKEN_INVALID);
        }
    }
}
