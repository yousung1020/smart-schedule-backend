package com.smartschedule.smartschedule.domain.member.service.query;

import com.smartschedule.smartschedule.domain.member.converter.MemberConverter;
import com.smartschedule.smartschedule.domain.member.dto.response.MemberResponseDTO;
import com.smartschedule.smartschedule.domain.member.entity.Member;
import com.smartschedule.smartschedule.domain.member.enums.SocialProvider;
import com.smartschedule.smartschedule.domain.member.exception.MemberException;
import com.smartschedule.smartschedule.domain.member.exception.code.error.MemberErrorCode;
import com.smartschedule.smartschedule.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberQueryService {
    private final MemberRepository memberRepository;

    public Optional<MemberResponseDTO.MemberResultDTO> findBySocialIdAndProvider(String socialId, SocialProvider provider) {
        log.info("소셜 ID로 회원을 조회합니다: provider={}, socialId={}", provider, socialId);
        return memberRepository.findBySocialIdAndSocialProvider(socialId, provider)
                .map(MemberConverter::toResultDTO);
    }

    public Member findByEmail(String email) {
        log.info("이메일로 회원을 조회합니다: email={}", email);
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    public Member findById(Long id) {
        log.info("ID로 회원을 조회합니다: id={}", id);
        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
