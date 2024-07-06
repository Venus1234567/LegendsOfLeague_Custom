package com.project.legendsofleague.domain.member.controller;
import com.project.legendsofleague.domain.member.service.RegisterService;
import com.project.legendsofleague.domain.member.dto.RegisterDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

      @PostMapping("/register")
    public void registerProc(@RequestBody RegisterDto dto){

          registerService.joinProcess(dto);
      }
}
