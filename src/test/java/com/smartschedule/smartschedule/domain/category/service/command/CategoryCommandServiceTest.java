package com.smartschedule.smartschedule.domain.category.service.command;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.smartschedule.smartschedule.domain.category.dto.request.CategoryRequestDTO;
import com.smartschedule.smartschedule.domain.category.dto.response.CategoryResponseDTO;
import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.category.exception.CategoryException;
import com.smartschedule.smartschedule.domain.category.exception.code.error.CategoryErrorCode;
import com.smartschedule.smartschedule.domain.category.repository.CategoryRepository;
import com.smartschedule.smartschedule.domain.category.service.query.CategoryQueryService;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CategoryCommandServiceTest {
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryQueryService categoryQueryService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ScheduleRepository scheduleRepository;

    @InjectMocks
    private CategoryCommandService categoryCommandService;

    private Member testMember;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testMember = Member.builder()
                .email("test@test.com")
                .password("password")
                .nickname("Tester")
                .role(Role.ROLE_USER)
                .isActive(true)
                .socialProvider(SocialProvider.KAKAO)
                .socialId("12345")
                .build();
        ReflectionTestUtils.setField(testMember, "id", 1L);

        testCategory = Category.builder()
                .name("기존 카테고리")
                .color("#000000")
                .member(testMember)
                .build();
        ReflectionTestUtils.setField(testCategory, "id", 100L);
    }

    @Test
    @DisplayName("카테고리 생성 성공 - Builder 패턴 적용 DTO 검증")
    void createCategory_Success() {
        // given
        CategoryRequestDTO.CategoryCreateDTO request = CategoryRequestDTO.CategoryCreateDTO.builder()
                .name("새 카테고리")
                .color("#FFFFFF")
                .build();

        // when
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember));
        when(categoryRepository.save(any(Category.class))).thenReturn(testCategory);

        CategoryResponseDTO.CategoryResultDTO result = categoryCommandService.createCategory(request, 1L);

        // then
        assertNotNull(result);
        assertEquals("기존 카테고리", result.name()); // testCategory의 name이 반환됨을 확인
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_Success() {
        // given
        CategoryRequestDTO.CategoryUpdateDTO request = CategoryRequestDTO.CategoryUpdateDTO.builder()
                .name("수정된 이름")
                .color("#FF0000")
                .build();

        when(categoryQueryService.findByIdAndMemberId(100L, 1L)).thenReturn(testCategory);

        // when
        CategoryResponseDTO.CategoryResultDTO result = categoryCommandService.updateCategory(100L, request, 1L);

        // then
        assertEquals("수정된 이름", testCategory.getName());
        assertEquals("#FF0000", testCategory.getColor());
        assertEquals("수정된 이름", result.name());
    }

    @Test
    @DisplayName("카테고리 삭제 성공 - 무결성 보장을 위한 벌크 연산 순차 호출 검증")
    void deleteCategory_Success() {
        // when
        when(categoryRepository.existsByIdAndMemberId(100L, 1L)).thenReturn(true);

        categoryCommandService.deleteCategory(100L, 1L);

        // then
        // 외래키 해제가 먼저 일어나는지 검증
        verify(scheduleRepository).setCategoryNullByCategoryId(100L);
        // 그 다음 벌크 삭제가 일어나는지 검증
        verify(categoryRepository).deleteBulkByIdAndMemberId(100L, 1L);
    }

    @Test
    @DisplayName("카테고리 삭제 실패 - 소유권 없음 (IDOR 방어 및 404 예외 발생)")
    void deleteCategory_Fail_NotFound() {
        // when
        when(categoryRepository.existsByIdAndMemberId(100L, 1L)).thenReturn(false);

        CategoryException exception = assertThrows(CategoryException.class,
                () -> categoryCommandService.deleteCategory(100L, 1L));

        // when
        assertEquals(CategoryErrorCode.CATEGORY_NOT_FOUND, exception.getCode());

        // 예외 발생 시 삭제 로직들이 절대로 호출되지 않아야 함
        verify(scheduleRepository, never()).setCategoryNullByCategoryId(anyLong());
        verify(categoryRepository, never()).deleteBulkByIdAndMemberId(anyLong(), anyLong());
    }
}
