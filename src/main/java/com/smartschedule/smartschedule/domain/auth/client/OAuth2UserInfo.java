package com.smartschedule.smartschedule.domain.auth.client;

public interface OAuth2UserInfo {
    String getSocialId();
    String getEmail();
    String getNickname();
}
