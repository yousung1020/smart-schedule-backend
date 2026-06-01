package com.smartschedule.smartschedule.domain.schedule.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.category.service.query.CategoryQueryService;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.notification.service.command.NotificationCommandService;
import com.smartschedule.smartschedule.domain.schedule.dto.request.ScheduleRequestDTO;
import com.smartschedule.smartschedule.domain.schedule.dto.response.ScheduleResponseDTO;
import com.smartschedule.smartschedule.domain.schedule.entity.Schedule;
import com.smartschedule.smartschedule.domain.schedule.enums.Priority;
import com.smartschedule.smartschedule.domain.schedule.exception.ScheduleException;
import com.smartschedule.smartschedule.domain.schedule.exception.code.error.ScheduleErrorCode;
import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import com.smartschedule.smartschedule.domain.schedule.service.query.ScheduleQueryService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ScheduleCommandServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ScheduleQueryService scheduleQueryService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CategoryQueryService categoryQueryService;
    @Mock
    private NotificationCommandService notificationCommandService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ScheduleCommandService scheduleCommandService;

    private Member testMember;
    private Category testCategory;
    private Schedule testSchedule;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("Tester")
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testCategory = Category.builder()
                .name("테스트 카테고리")
                .color("#FFFFFF")
                .member(testMember)
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 100L);

        testSchedule = Schedule.builder()
                .title("테스트 일정")
                .content("내용")
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(1).plusHours(2))
                .priority(Priority.HIGH)
                .isCompleted(false)
                .member(testMember)
                .category(testCategory)
                .build();
        ReflectionTestUtils.setField(testSchedule, "id", 1000L);
    }

    @Test
    @DisplayName("일정 생성 성공 검증")
    void createSchedule_Success() {
        // given
        ScheduleRequestDTO.ScheduleCreateDTO request = createTestScheduleCreateDTO();

        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryQueryService.findByIdAndMemberId(100L, 1L)).thenReturn(testCategory);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(testSchedule);

        // when
        ScheduleResponseDTO.ScheduleResultDTO result = scheduleCommandService.createSchedule(request, 1L);

        // then
        assertNotNull(result);
        assertEquals("테스트 일정", result.title());
        verify(scheduleRepository).save(any(Schedule.class));
        verify(notificationCommandService).createNotifications(any(Schedule.class), eq(List.of(60, 30)));
    }

    @Test
    @DisplayName("일정 생성 실패 - 존재하지 않는 회원 조회 시 예외 발생 검증")
    void createSchedule_Fail_MemberNotFound() {
        // given
        ScheduleRequestDTO.ScheduleCreateDTO request = createTestScheduleCreateDTO();

        // 회원을 찾지 못해 빈 Optional 반환 상황
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        MemberException exception = assertThrows(
                MemberException.class,
                () -> scheduleCommandService.createSchedule(request, 1L)
        );

        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, exception.getCode());
        verify(scheduleRepository, never()).save(any());
        verify(notificationCommandService, never()).createNotifications(any(), any());
    }

    @Test
    @DisplayName("일정 생성 실패 - 기간 역전 논리적 오류 검증 (INVALID_SCHEDULE_DATE)")
    void createSchedule_Fail_InvalidDate() {
        // given
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.minusHours(1); // 종료 시간이 시작 시간보다 앞섬

        ScheduleRequestDTO.ScheduleCreateDTO request = createTestScheduleCreateDTO(startTime, endTime);

        // when & then
        ScheduleException exception = assertThrows(ScheduleException.class,
                () -> scheduleCommandService.createSchedule(request, 1L));

        assertEquals(ScheduleErrorCode.INVALID_SCHEDULE_DATE, exception.getCode());
        verify(scheduleRepository, never()).save(any());
        verify(notificationCommandService, never()).createNotifications(any(), any());
    }

    @Test
    @DisplayName("일정 수정 성공 검증")
    void updateSchedule_Success() {
        // given
        LocalDateTime newStartTime = LocalDateTime.now().plusDays(2);
        LocalDateTime newEndTime = newStartTime.plusHours(2);

        ScheduleRequestDTO.ScheduleUpdateDTO request = ScheduleRequestDTO.ScheduleUpdateDTO.builder()
                .title("수정된 일정")
                .content("수정된 내용")
                .startTime(newStartTime)
                .endTime(newEndTime)
                .priority(Priority.LOW)
                .categoryId(100L)
                .notifyBeforeMinutes(List.of(15))
                .build();

        when(scheduleQueryService.findByIdAndMemberId(1000L, 1L)).thenReturn(testSchedule);
        when(categoryQueryService.findByIdAndMemberId(100L, 1L)).thenReturn(testCategory);

        // when
        ScheduleResponseDTO.ScheduleResultDTO result = scheduleCommandService.updateSchedule(1000L, request, 1L);

        // then
        assertEquals("수정된 일정", testSchedule.getTitle());
        assertEquals("수정된 내용", testSchedule.getContent());
        assertEquals(Priority.LOW, testSchedule.getPriority());
        assertEquals("수정된 일정", result.title());
        verify(notificationCommandService).updateNotifications(testSchedule, List.of(15));
    }

    @Test
    @DisplayName("일정 완료 여부 변경 성공 검증")
    void updateScheduleCompletion_Success() {
        // given
        when(scheduleQueryService.findByIdAndMemberId(1000L, 1L)).thenReturn(testSchedule);

        // when
        ScheduleResponseDTO.ScheduleResultDTO result = scheduleCommandService.updateScheduleCompletion(
                1000L,
                true,
                1L
        );

        // then
        assertTrue(testSchedule.isCompleted());
        assertTrue(result.isCompleted());
    }

    @Test
    @DisplayName("일정 삭제 성공 검증")
    void deleteSchedule_Success() {
        // given
        when(scheduleQueryService.findByIdAndMemberId(1000L, 1L)).thenReturn(testSchedule);

        // when
        scheduleCommandService.deleteSchedule(1000L, 1L);

        // then
        verify(notificationCommandService).deleteNotificationsByScheduleId(1000L);
        verify(scheduleRepository).delete(testSchedule);
    }

    private ScheduleRequestDTO.ScheduleCreateDTO createTestScheduleCreateDTO() {
        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);
        return createTestScheduleCreateDTO(startTime, endTime);
    }

    private ScheduleRequestDTO.ScheduleCreateDTO createTestScheduleCreateDTO(LocalDateTime startTime,
                                                                             LocalDateTime endTime) {
        return ScheduleRequestDTO.ScheduleCreateDTO.builder()
                .title("새 일정")
                .content("새 내용")
                .startTime(startTime)
                .endTime(endTime)
                .priority(Priority.MEDIUM)
                .categoryId(100L)
                .notifyBeforeMinutes(List.of(60, 30))
                .build();
    }
}
