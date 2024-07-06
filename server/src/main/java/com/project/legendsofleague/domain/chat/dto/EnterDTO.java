package com.project.legendsofleague.domain.chat.dto;

import lombok.Data;

import java.util.List;

/**
 * 채팅방 입장 정보를 담은 DTO입니다.
 */
@Data
public class EnterDTO {

    private String username;
    private List<ChatDto> previousChat;

}
