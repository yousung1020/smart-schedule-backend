package com.smartschedule.smartschedule.domain.schedule.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.smartschedule.smartschedule.domain.category.entity.QCategory;
import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.QSchedule;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.domain.schedule.enums.Priority;
import com.smartschedule.smartschedule.domain.statistics.dto.response.StatisticsResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ScheduleRepositoryImpl implements ScheduleRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private final QSchedule schedule = QSchedule.schedule;
    private final QCategory category = QCategory.category;

    @Override
    public List<Schedule> findCalendarSchedules(
        Long memberId,
        ScheduleRequestDTO.CalendarSearch condition
    ) {
        return queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.category, category).fetchJoin()
                .where(
                        memberIdEq(memberId),
                        dateBetween(condition.startDate(), condition.endDate()),
                        categoryIdEq(condition.categoryId())
                )
                .orderBy(schedule.startTime.asc())
                .fetch();
    }

    @Override
    public Page<Schedule> searchSchedules(
        Long memberId,
        ScheduleRequestDTO.ListSearch condition,
        Pageable pageable
    ) {
        OrderSpecifier<?>[] orderSpecifiers = getOrderSpecifiers(pageable);

        List<Schedule> content = queryFactory
                .selectFrom(schedule)
                .leftJoin(schedule.category, category).fetchJoin()
                .where(
                        memberIdEq(memberId),
                        keywordContains(condition.keyword()),
                        priorityEq(condition.priority()),
                        isCompletedEq(condition.isCompleted()),
                        categoryIdEq(condition.categoryId())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(orderSpecifiers)
                .fetch();

        Long totalCount = queryFactory
                .select(schedule.count())
                .from(schedule)
                .where(
                        memberIdEq(memberId),
                        keywordContains(condition.keyword()),
                        priorityEq(condition.priority()),
                        isCompletedEq(condition.isCompleted()),
                        categoryIdEq(condition.categoryId())
                )
                .fetchOne();

        long total = (totalCount != null) ? totalCount : 0L;

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public StatisticsResponseDTO.CompletionRateResultDTO getCompletionRate(
        Long memberId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        Long totalCount = queryFactory
                .select(schedule.count())
                .from(schedule)
                .where(
                        memberIdEq(memberId),
                        schedule.startTime.between(startDate, endDate))
                .fetchOne();

        Long doneCount = queryFactory
                .select(schedule.count())
                .from(schedule)
                .where(
                        memberIdEq(memberId),
                        schedule.createdAt.between(startDate, endDate),
                        schedule.isCompleted.isTrue())
                .fetchOne();

        long total = totalCount != null ? totalCount : 0L;
        long done = doneCount != null ? doneCount : 0L;
        double rate = total == 0 ? 0.0 : ((double) done / total) * 100.0;

        return StatisticsResponseDTO.CompletionRateResultDTO.builder()
                .completionRate(rate)
                .totalCount(total)
                .doneCount(done)
                .build();
    }

    @Override
    public List<StatisticsResponseDTO.CategoryDistributionResultDTO> getCategoryDistribution(
        Long memberId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        return queryFactory
                .select(
                    Projections.constructor(
                        StatisticsResponseDTO.CategoryDistributionResultDTO.class,
                        schedule.category.id,
                        schedule.category.name,
                        schedule.count(),
                        Expressions.constant(0.0)
                    )
                )
                .from(schedule)
                .leftJoin(schedule.category, category)
                .where(
                        memberIdEq(memberId),
                        schedule.startTime.between(startDate, endDate))
                .groupBy(schedule.category.id, schedule.category.name)
                .fetch();
    }

    @Override
    public List<StatisticsResponseDTO.WeeklyActivityResultDTO> getWeeklyActivity(
        Long memberId,
        LocalDateTime startDate,
        LocalDateTime endDate
    ) {
        StringExpression yearWeek = Expressions.stringTemplate("CONCAT(YEAR({0}), '-', WEEK({0}))", schedule.createdAt);

        return queryFactory
                .select(Projections.constructor(StatisticsResponseDTO.WeeklyActivityResultDTO.class,
                        yearWeek,
                        schedule.count()))
                .from(schedule)
                .where(
                        memberIdEq(memberId),
                        schedule.createdAt.between(startDate, endDate))
                .groupBy(yearWeek)
                .orderBy(yearWeek.asc())
                .fetch();
    }

    private OrderSpecifier<?>[] getOrderSpecifiers(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return new OrderSpecifier<?>[] { schedule.createdAt.desc() };
        }

        return pageable.getSort().stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    String property = order.getProperty();
                    PathBuilder<Schedule> pathBuilder = new PathBuilder<>(Schedule.class, "schedule");
                    return new OrderSpecifier(direction, pathBuilder.get(property));
                })
                .toArray(OrderSpecifier[]::new);
    }

    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? schedule.member.id.eq(memberId) : null;
    }

    private BooleanExpression dateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            return null;
        }
        return schedule.startTime.loe(endDate).and(schedule.endTime.goe(startDate));
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? schedule.category.id.eq(categoryId) : null;
    }

    private BooleanExpression keywordContains(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return schedule.title.containsIgnoreCase(keyword)
                .or(schedule.content.containsIgnoreCase(keyword));
    }

    private BooleanExpression priorityEq(Priority priority) {
        return priority != null ? schedule.priority.eq(priority) : null;
    }

    private BooleanExpression isCompletedEq(Boolean isCompleted) {
        return isCompleted != null ? schedule.isCompleted.eq(isCompleted) : null;
    }
}
