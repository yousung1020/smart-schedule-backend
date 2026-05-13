package com.smartschedule.smartschedule.global.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheVersionService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String VERSION_KEY_PREFIX = "cache:version:";

    // 특정 도메인의 사용자별 캐시 버전 조회
    public String getVersion(
            String domain,
            Long memberId
    ) {
        String key = buildKey(domain, memberId);
        Object version = redisTemplate.opsForValue().get(key);
        
        if (version == null) {
            String initialVersion = String.valueOf(System.currentTimeMillis());
            redisTemplate.opsForValue().set(
                key,
                initialVersion,
                Duration.ofDays(1)
            );
            return initialVersion;
        }
        
        return version.toString();
    }

    // 특정 도메인의 사용자별 캐시 버전 갱신
    public void updateVersion(
            String domain,
            Long memberId
    ) {
        log.info("캐시 버전을 갱신합니다: domain={}, memberId={}", domain, memberId);
        String key = buildKey(domain, memberId);
        redisTemplate.opsForValue().set(
            key,
            String.valueOf(System.currentTimeMillis()),
            Duration.ofDays(1)
        );
    }

    private String buildKey(String domain, Long memberId) {
        return VERSION_KEY_PREFIX + domain + ":" + memberId;
    }
}
