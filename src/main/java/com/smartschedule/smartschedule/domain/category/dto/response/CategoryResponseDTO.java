package com.smartschedule.smartschedule.domain.category.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

public class CategoryResponseDTO {

    @Builder
    public record CategoryResultDTO(
            Long id,
            String name,
            String color,
            LocalDateTime createdAt
    ) {}

    @Builder
    public record CategoryListResultDTO(
            List<CategoryResultDTO> categories
    ) {}
}
