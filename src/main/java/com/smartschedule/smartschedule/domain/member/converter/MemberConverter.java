package com.smartschedule.smartschedule.domain.member.converter;

import com.smartschedule.smartschedule.domain.auth.dto.request.AuthRequestDTO;
import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;

public class MemberConverter {
    public static MemberResponseDTO.MemberResultDTO toResultDTO(Member member) {
        return MemberResponseDTO.MemberResultDTO.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .role(member.getRole())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public static Member toEntity(AuthRequestDTO.SignupDTO request, String encodedPassword) {
        return Member.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .nickname(request.getNickname())
                .role(Role.ROLE_USER)
                .isActive(true)
                .build();
    }

    public static Member toSocialEntity(String email, String nickname, String socialId, SocialProvider provider, String encodedPassword) {
        return Member.builder()
                .email(email)
                .password(encodedPassword)
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .isActive(true)
                .socialProvider(provider)
                .socialId(socialId)
                .build();
    }
}
