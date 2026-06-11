package com.smartschedule.smartschedule.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class CategoryRequestDTO {
    @Builder
    public record CategoryCreateDTO(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(max = 50, message = "카테고리 이름은 50자 이내여야 합니다.")
            String name,

            @Size(max = 20, message = "색상 코드는 20자 이내여야 합니다.")
            String color
    ) {}

    @Builder
    public record CategoryUpdateDTO(
            @NotBlank(message = "카테고리 이름은 필수입니다.")
            @Size(max = 50, message = "카테고리 이름은 50자 이내여야 합니다.")
            String name,

            @Size(max = 20, message = "색상 코드는 20자 이내여야 합니다.")
            String color
    ) {}
}
