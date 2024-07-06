package com.project.legendsofleague.domain.member.service;

import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.member.dto.RegisterDto;
import com.project.legendsofleague.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterService {

    private final MemberRepository memberRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public void joinProcess(RegisterDto dto) {

        RegisterDto.validate(dto);

        Boolean checkUser = memberRepository.existsByUsername(dto.getUsername());

        if (!checkUser) {
            Member member = Member.from(dto, bCryptPasswordEncoder);
            memberRepository.save(member);
        }

    }
}
