package com.project.legendsofleague.domain.chat.controller;

import com.project.legendsofleague.domain.chat.dto.ChatDto;
import com.project.legendsofleague.domain.chat.dto.EnterDTO;
import com.project.legendsofleague.domain.chat.repository.ChatRepository;
import com.project.legendsofleague.domain.member.domain.CurrentMember;
import com.project.legendsofleague.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@Slf4j
@Tag(name= "채팅", description = "채팅 관련 API")
@RequiredArgsConstructor
public class ChatController {


    private final SimpMessageSendingOperations template;


    private final ChatRepository repository;

    /**
     * 채팅 서버에 입장한 사용자의 정보와 이전 채팅 기록을 반환합니다.
     *
     * @return EnterDTO(사용자명, 이전 채팅 리스트)
     */
    @GetMapping("/chat/enterUser")
    @Operation(summary = "채팅 입장 시 사용자 정보 조회")
    public ResponseEntity<EnterDTO> enterUser(@CurrentMember Member member) {
        log.info("enterUser 호출");

        EnterDTO enterDTO = new EnterDTO();

        List<ChatDto> previousChats = repository.findPreviousChats();
        enterDTO.setPreviousChat(previousChats);
        enterDTO.setUsername(member.getUsername());

        return new ResponseEntity<>(enterDTO, HttpStatus.OK);
    }

    /**
     * WebSocket 으로 받은 메시지를 그대로 전송하는 메서드입니다.
     *
     * @param chat WebSocket 으로 받은 채팅 정보
     */
    @MessageMapping("/chat/sendMessage")
    public void sendMessage(@Payload ChatDto chat) {
        log.info("chatDto = {}", chat);

        chat.setTime(LocalDateTime.now());
        repository.save(chat);

        template.convertAndSend("/sub/chat", chat);
    }

}
