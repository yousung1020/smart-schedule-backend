package com.smartschedule.smartschedule.global.auth;

import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import java.util.Collection;
import java.util.List;
import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record CustomUserDetails(
        MemberResponseDTO.MemberResultDTO memberDTO
) implements UserDetails {
    public Long getMemberId() {
        return memberDTO.id();
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> memberDTO.role().toString());
    }

    @Override
    public String getPassword() {
        return "";
    }

    @NonNull
    @Override
    public String getUsername() {
        return String.valueOf(memberDTO.id());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
