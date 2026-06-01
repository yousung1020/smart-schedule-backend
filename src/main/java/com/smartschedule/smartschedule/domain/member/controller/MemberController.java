package com.smartschedule.smartschedule.domain.member.controller;

import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.exception.code.success.MemberSuccessCode;
import com.smartschedule.smartschedule.domain.member.service.command.MemberCommandService;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {
    private final MemberCommandService memberCommandService;

    @GetMapping("/me")
    public ApiResponse<MemberResponseDTO.MemberResultDTO> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_INFO_FETCH_SUCCESS, userDetails.memberDTO());
    }

    @DeleteMapping("/me")
    public ApiResponse<String> withdrawMember(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        memberCommandService.withdrawMember(userDetails.getMemberId());
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_WITHDRAWAL_SUCCESS, "회원 탈퇴가 완료되었습니다.");
    }
}
