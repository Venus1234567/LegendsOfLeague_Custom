package com.project.legendsofleague.domain.chat.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Builder
public class ChatDto {

    private MessageType type;
    private String roomId;
    private String sender;
    private String content;
    private LocalDateTime time;

}
