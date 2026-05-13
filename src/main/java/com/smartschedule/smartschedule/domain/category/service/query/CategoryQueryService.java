package com.smartschedule.smartschedule.domain.category.service.query;

import com.smartschedule.smartschedule.domain.category.converter.CategoryConverter;
import com.smartschedule.smartschedule.domain.category.dto.response.CategoryResponseDTO;
import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.category.exception.CategoryException;
import com.smartschedule.smartschedule.domain.category.exception.code.error.CategoryErrorCode;
import com.smartschedule.smartschedule.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    // 회원의 전체 카테고리 목록 조회
    public CategoryResponseDTO.CategoryListResultDTO getCategoryList(Long memberId) {
        log.info("회원의 카테고리 목록을 조회합니다: memberId={}", memberId);
        List<Category> categories = categoryRepository.findAllByMemberId(memberId);
        return CategoryConverter.toListResultDTO(categories);
    }

    // 카테고리 엔티티 조회(서비스단 조회용)
    public Category findByIdAndMemberId(Long categoryId, Long memberId) {
        return categoryRepository.findByIdAndMemberId(categoryId, memberId)
                .orElseThrow(() -> new CategoryException(CategoryErrorCode.CATEGORY_NOT_FOUND));
    }
}
