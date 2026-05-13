package com.smartschedule.smartschedule.domain.member.dto.response;

import java.time.LocalDateTime;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import lombok.Builder;

public class MemberResponseDTO {
    @Builder
    public record MemberResultDTO(
            Long id,
            String email,
            String nickname,
            Role role,
            LocalDateTime createdAt
    ) {}
}
