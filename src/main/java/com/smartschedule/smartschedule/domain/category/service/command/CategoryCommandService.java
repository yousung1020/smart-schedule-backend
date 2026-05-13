package com.smartschedule.smartschedule.domain.category.service.command;

import com.smartschedule.smartschedule.domain.category.converter.CategoryConverter;
import com.smartschedule.smartschedule.domain.category.dto.request.CategoryRequestDTO;
import com.smartschedule.smartschedule.domain.category.dto.response.CategoryResponseDTO;
import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.category.exception.CategoryException;
import com.smartschedule.smartschedule.domain.category.exception.code.error.CategoryErrorCode;
import com.smartschedule.smartschedule.domain.category.repository.CategoryRepository;
import com.smartschedule.smartschedule.domain.category.service.query.CategoryQueryService;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryCommandService {

    private final CategoryRepository categoryRepository;
    private final CategoryQueryService categoryQueryService;
    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;

    // 카테고리 생성
    public CategoryResponseDTO.CategoryResultDTO createCategory(
            CategoryRequestDTO.CategoryCreateDTO request,
            Long memberId
    ) {
        log.info("카테고리 생성을 시작합니다: memberId={}, name={}", memberId, request.name());
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Category category = CategoryConverter.toEntity(request, member);
        Category savedCategory = categoryRepository.save(category);

        log.info("카테고리가 성공적으로 생성되었습니다: categoryId={}, memberId={}", savedCategory.getId(), memberId);
        return CategoryConverter.toResultDTO(savedCategory);
    }

    // 카테고리 수정
    public CategoryResponseDTO.CategoryResultDTO updateCategory(
            Long categoryId,
            CategoryRequestDTO.CategoryUpdateDTO request,
            Long memberId
    ) {
        log.info("카테고리 수정을 시작합니다: categoryId={}, memberId={}, newName={}", categoryId, memberId, request.name());
        Category category = categoryQueryService.findByIdAndMemberId(categoryId, memberId);
        category.update(request.name(), request.color());

        log.info("카테고리가 성공적으로 수정되었습니다: categoryId={}, memberId={}", categoryId, memberId);
        return CategoryConverter.toResultDTO(category);
    }

    // 카테고리 삭제
    public void deleteCategory(Long categoryId, Long memberId) {
        log.info("카테고리 삭제를 시작합니다: categoryId={}, memberId={}", categoryId, memberId);
        // 존재 및 소유권 확인 (SELECT 1번, 엔티티 로드X)
        if (!categoryRepository.existsByIdAndMemberId(categoryId, memberId)) {
            throw new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND);
        }

        // 외래 키 제약 조건 방지 벌크 업데이트 (UPDATE 1번)
        scheduleRepository.setCategoryNullByCategoryId(categoryId);

        // 영속성 컨텍스트 우회 벌크 삭제 (DELETE 1번)
        categoryRepository.deleteBulkByIdAndMemberId(categoryId, memberId);
        log.info("카테고리가 성공적으로 삭제되었습니다: categoryId={}, memberId={}", categoryId, memberId);
    }
}
