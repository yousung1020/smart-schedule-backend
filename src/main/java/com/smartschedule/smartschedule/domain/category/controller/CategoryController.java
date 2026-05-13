package com.smartschedule.smartschedule.domain.category.controller;

import com.smartschedule.smartschedule.domain.category.dto.request.CategoryRequestDTO;
import com.smartschedule.smartschedule.domain.category.dto.response.CategoryResponseDTO;
import com.smartschedule.smartschedule.domain.category.exception.code.success.CategorySuccessCode;
import com.smartschedule.smartschedule.domain.category.service.command.CategoryCommandService;
import com.smartschedule.smartschedule.domain.category.service.query.CategoryQueryService;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.auth.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryCommandService categoryCommandService;
    private final CategoryQueryService categoryQueryService;

    // 카테고리 목록 조회
    @GetMapping
    public ApiResponse<CategoryResponseDTO.CategoryListResultDTO> getCategoryList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CategoryResponseDTO.CategoryListResultDTO result = categoryQueryService.getCategoryList(userDetails.getMemberId());
        
        return ApiResponse.onSuccess(CategorySuccessCode.CATEGORY_LIST_FETCH_SUCCESS, result);
    }

    // 카테고리 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // HTTP 201 상태 코드 명시
    public ApiResponse<CategoryResponseDTO.CategoryResultDTO> createCategory(
            @RequestBody @Valid CategoryRequestDTO.CategoryCreateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CategoryResponseDTO.CategoryResultDTO result = categoryCommandService.createCategory(request, userDetails.getMemberId());
        
        return ApiResponse.onSuccess(CategorySuccessCode.CATEGORY_CREATE_SUCCESS, result);
    }

    // 카테고리 수정
    @PatchMapping("/{categoryId}")
    public ApiResponse<CategoryResponseDTO.CategoryResultDTO> updateCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestBody @Valid CategoryRequestDTO.CategoryUpdateDTO request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        CategoryResponseDTO.CategoryResultDTO result = categoryCommandService.updateCategory(categoryId, request, userDetails.getMemberId());
        
        return ApiResponse.onSuccess(CategorySuccessCode.CATEGORY_UPDATE_SUCCESS, result);
    }

    // 카테고리 삭제
    @DeleteMapping("/{categoryId}")
    public ApiResponse<Void> deleteCategory(
            @PathVariable("categoryId") Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        categoryCommandService.deleteCategory(categoryId, userDetails.getMemberId());
        
        return ApiResponse.onSuccess(CategorySuccessCode.CATEGORY_DELETE_SUCCESS, null);
    }
}
