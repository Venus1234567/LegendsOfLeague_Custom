package com.project.legendsofleague.domain.member.service;

import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.member.dto.CustomOAuth2Member;
import com.project.legendsofleague.domain.member.dto.GoogleResponse;
import com.project.legendsofleague.domain.member.dto.MemberDto;
import com.project.legendsofleague.domain.member.dto.OAuth2Response;
import com.project.legendsofleague.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2MemberService extends DefaultOAuth2UserService {
    private final MemberRepository memberRepository;


    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId =
                userRequest
                        .getClientRegistration()
                        .getRegistrationId();

        OAuth2Response oAuth2Response = null;


        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }

        String username = oAuth2Response.getProvider() + " " + oAuth2Response.getProviderId();
        Member existMember = memberRepository.findByUsername(username);

        if (existMember == null) {

            Member member = Member.create(username, oAuth2Response.getEmail(), oAuth2Response.getName());
            memberRepository.save(member);

            MemberDto memberDto = MemberDto.of(username, oAuth2Response.getName());

            return new CustomOAuth2Member(memberDto);
        } else {

            existMember.updateUser(oAuth2Response);
            memberRepository.save(existMember);

            MemberDto memberDto = MemberDto.of(username, oAuth2Response.getName());

            return new CustomOAuth2Member(memberDto);

        }


    }
}
