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
        if (memberRepository.existsByEmail(request.email())) {
            throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
        }
        String encodedPassword = passwordEncoder.encode(request.password());
        Member newMember = MemberConverter.toEntity(request, encodedPassword);

        return MemberConverter.toResultDTO(memberRepository.save(newMember));
    }

    // 소셜 회원가입
    public MemberResponseDTO.MemberResultDTO createSocialMember(String email, String nickname, String socialId, SocialProvider provider) {
        // 이미 동일한 소셜 ID와 제공자 조합으로 가입된 정보가 있는지 검증
        memberRepository.findBySocialIdAndSocialProvider(socialId, provider).ifPresent(existingMember -> {
            throw new MemberException(MemberErrorCode.INVALID_SOCIAL_PROVIDER);
        });

        // 이미 가입된 이메일이 있는지 검증
        memberRepository.findByEmail(email).ifPresent(existingMember -> {
            if (existingMember.getSocialProvider() == null) {
                throw new MemberException(MemberErrorCode.DUPLICATE_EMAIL);
            } else {
                throw new MemberException(MemberErrorCode.INVALID_SOCIAL_PROVIDER);
            }
        });

        String randomPassword = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(randomPassword);
        Member newMember = MemberConverter.toSocialEntity(email, nickname, socialId, provider, encodedPassword);

        Member savedMember = memberRepository.save(newMember);
        log.info("새로운 소셜 회원이 가입되었습니다: provider={}, email={}", provider, savedMember.getEmail());
        return MemberConverter.toResultDTO(savedMember);
    }

    // 비밀번호 업데이트
    public void updatePassword(Member member, String encodedPassword) {
        member.updatePassword(encodedPassword);
        log.info("사용자의 비밀번호가 변경되었습니다: email={}", member.getEmail());
    }

    // 회원 탈퇴
    public void withdrawMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        
        log.info("사용자가 탈퇴 처리되었습니다: email={}", member.getEmail());
    }
}
