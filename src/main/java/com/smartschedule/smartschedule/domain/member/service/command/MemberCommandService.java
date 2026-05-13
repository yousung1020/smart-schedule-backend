package com.smartschedule.smartschedule.domain.member.service.command;

import com.smartschedule.smartschedule.domain.auth.dto.request.AuthRequestDTO;
import com.smartschedule.smartschedule.domain.member.converter.MemberConverter;
import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.entity.Member;
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

    // 일반 회원가입
    public MemberResponseDTO.MemberResultDTO createMember(AuthRequestDTO.SignupDTO request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Member newMember = MemberConverter.toEntity(request, encodedPassword);

        return MemberConverter.toResultDTO(memberRepository.save(newMember));
    }

    // 소셜 회원가입
    public MemberResponseDTO.MemberResultDTO createSocialMember(String email, String nickname, String socialId, SocialProvider provider) {
        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        Member newMember = MemberConverter.toSocialEntity(email, nickname, socialId, provider, encodedPassword);

        try {
        Member savedMember = memberRepository.save(newMember);
        log.info("새로운 소셜 회원이 가입되었습니다: provider={}, email={}", provider, savedMember.getEmail());
        return MemberConverter.toResultDTO(savedMember);
        } catch (DataIntegrityViolationException e) {
            log.warn("동시성 이슈 감지: 이미 가입된 소셜 회원입니다. provider={}, socialId={}", provider, socialId);
            Member existingMember = memberRepository.findBySocialIdAndSocialProvider(socialId, provider)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
            return MemberConverter.toResultDTO(existingMember);
        }
    }

    // 비밀번호 업데이트
    public void updatePassword(Member member, String encodedPassword) {
        member.updatePassword(encodedPassword);
        log.info("사용자의 비밀번호가 변경되었습니다: email={}", member.getEmail());
    }
}
