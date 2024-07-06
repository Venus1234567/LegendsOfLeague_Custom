package com.project.legendsofleague.domain.member.dto;


import com.project.legendsofleague.domain.member.domain.ROLE;
import lombok.Getter;

@Getter
public class MemberDto {

    private String role;
    private String name;
    private String username;

    public static MemberDto of(OAuth2Response oAuth2Response, String username){
        MemberDto memberDto = new MemberDto();
        memberDto.role = ROLE.ROLE_USER.toString();
        memberDto.name = oAuth2Response.getName();
        memberDto.username = username;

        return memberDto;
    }

    public static MemberDto of(String username, String nickname){
        MemberDto memberDto = new MemberDto();
        memberDto.username = username;
        memberDto.name = nickname;
        memberDto.role = ROLE.ROLE_USER.toString();

        return memberDto;
    }

}
