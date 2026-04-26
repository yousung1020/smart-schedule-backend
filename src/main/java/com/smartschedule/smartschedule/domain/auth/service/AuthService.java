package com.smartschedule.smartschedule.domain.auth.service;

import com.smartschedule.smartschedule.domain.auth.client.KakaoOAuthClient;
import com.smartschedule.smartschedule.domain.auth.dto.OAuth2UserInfo;
import com.smartschedule.smartschedule.domain.auth.dto.request.AuthRequestDTO;
import com.smartschedule.smartschedule.domain.auth.dto.response.AuthResponseDTO;
import com.smartschedule.smartschedule.domain.auth.dto.response.KakaoTokenResponseDTO;
import com.smartschedule.smartschedule.domain.auth.exception.AuthException;
import com.smartschedule.smartschedule.domain.auth.exception.code.error.AuthErrorCode;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.member.service.command.MemberCommandService;
import com.smartschedule.smartschedule.domain.member.service.query.MemberQueryService;
import com.smartschedule.smartschedule.global.util.JwtUtil;
import com.smartschedule.smartschedule.global.util.RedisUtil;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    // RT: Refresh Token의 줄임말
    private static final String REDIS_RT_PREFIX = "RT:";
    private static final String BEARER_PREFIX_LOWER = "bearer ";

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;
    private final MemberRepository memberRepository;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;

    private final KakaoOAuthClient kakaoOAuthClient;

    // 소셜 로그인
    public AuthResponseDTO.TokenResultDTO socialLogin(
            String provider,
            AuthRequestDTO.SocialLoginDTO request
    ) {
        SocialProvider socialProvider = SocialProvider.fromString(provider);

        // 제공자별 클라이언트를 통해 추상화된 유저 정보 얻기
        OAuth2UserInfo userInfo = fetchUserInfo(socialProvider, request.getAuthorizationCode());

        String socialId = userInfo.getSocialId();
        String email = userInfo.getEmail();
        String nickname = userInfo.getNickname();

        // 회원 조회 및 가입 처리
        Member member = memberQueryService.findBySocialIdAndProvider(socialId, socialProvider)
                .orElseGet(() -> memberCommandService.createSocialMember(email, nickname, socialId, socialProvider));

        return generateAndSaveTokens(member);
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

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.TOKEN_INVALID));

        return generateAndSaveTokens(member);
    }

    // 소셜 제공자별로 유저 정보를 가져오는 로직을 분리하여 확장성을 확보합니다.
    private OAuth2UserInfo fetchUserInfo(SocialProvider provider, String code) {
        return switch (provider) {
            case KAKAO -> {
                KakaoTokenResponseDTO tokenResponse = kakaoOAuthClient.fetchKakaoAccessToken(code);
                yield kakaoOAuthClient.fetchKakaoUserInfo(tokenResponse.accessToken());
            }
            // 추후에 네이버, 구글 등 추가(시간 되면..?)
            default -> throw new AuthException(AuthErrorCode.UNSUPPORTED_PROVIDER);
        };
    }

    private AuthResponseDTO.TokenResultDTO generateAndSaveTokens(Member member) {
        String accessToken = jwtUtil.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtUtil.createRefreshToken(member.getId());

        Long expirationMillis = jwtUtil.getExpirationTime(refreshToken);
        redisUtil.set(REDIS_RT_PREFIX + member.getId(), refreshToken, Duration.ofMillis(expirationMillis));

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
