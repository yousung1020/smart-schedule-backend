package com.smartschedule.smartschedule.domain.member.service.command;

import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.Role;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import com.smartschedule.smartschedule.domain.member.service.query.MemberQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberQueryService memberQueryService;

    public Member createSocialMember(String email, String nickname, String socialId, SocialProvider provider) {
        String randomPassword = UUID.randomUUID().toString();
        
        Member newMember = Member.builder()
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .nickname(nickname)
                .role(Role.ROLE_USER)
                .isActive(true)
                .socialProvider(provider)
                .socialId(socialId)
                .build();
                
        try {
            return memberRepository.save(newMember);
        } catch (DataIntegrityViolationException e) {
            log.warn("동시성 이슈 감지: 이미 가입된 소셜 회원입니다. provider={}, socialId={}", provider, socialId);
            return memberQueryService.findBySocialIdAndProvider(socialId, provider)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        }
    }
}

