package com.smartschedule.smartschedule.domain.statistics.listener;

import com.smartschedule.smartschedule.domain.schedule.event.ScheduleChangedEvent;
import com.smartschedule.smartschedule.global.cache.CacheVersionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsCacheListener {

    private final CacheVersionService cacheVersionService;

    // 일정이 변경되면 해당 사용자의 통계 캐시 버전을 갱신
    @EventListener
    public void handleScheduleChanged(
            ScheduleChangedEvent event
    ) {
        log.info("일정 변경 이벤트를 수신하여 통계 캐시 버전을 갱신합니다: memberId={}", event.memberId());
        cacheVersionService.updateVersion("stats", event.memberId());
    }
}
