package com.smartschedule.smartschedule.domain.member.controller;

import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.exception.code.success.MemberSuccessCode;
import com.smartschedule.smartschedule.global.apiPayload.ApiResponse;
import com.smartschedule.smartschedule.global.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {
    @GetMapping("/me")
    public ApiResponse<MemberResponseDTO.MemberResultDTO> getMyInfo(
        @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ApiResponse.onSuccess(MemberSuccessCode.MEMBER_INFO_FETCH_SUCCESS, userDetails.memberDTO());
    }
}
