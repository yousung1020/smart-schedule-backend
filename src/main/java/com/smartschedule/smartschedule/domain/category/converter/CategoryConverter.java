package com.smartschedule.smartschedule.domain.category.converter;

import com.smartschedule.smartschedule.domain.category.dto.request.CategoryRequestDTO;
import com.smartschedule.smartschedule.domain.category.dto.response.CategoryResponseDTO;
import com.smartschedule.smartschedule.domain.category.entity.Category;
import com.smartschedule.smartschedule.domain.member.entity.Member;

import java.util.List;

public class CategoryConverter {

    public static Category toEntity(
            CategoryRequestDTO.CategoryCreateDTO request,
            Member member
    ) {
        return Category.builder()
                .name(request.name())
                .color(request.color())
                .member(member)
                .build();
    }

    public static CategoryResponseDTO.CategoryResultDTO toResultDTO(Category category) {
        return CategoryResponseDTO.CategoryResultDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .color(category.getColor())
                .createdAt(category.getCreatedAt())
                .build();
    }

    public static CategoryResponseDTO.CategoryListResultDTO toListResultDTO(List<Category> categories) {
        List<CategoryResponseDTO.CategoryResultDTO> categoryResultDTOList = categories.stream()
                .map(CategoryConverter::toResultDTO)
                .toList();

        return CategoryResponseDTO.CategoryListResultDTO.builder()
                .categories(categoryResultDTOList)
                .build();
    }
}
