package com.smartschedule.smartschedule.domain.auth.dto;

public interface OAuth2UserInfo {
    String getSocialId();
    String getEmail();
    String getNickname();
}
