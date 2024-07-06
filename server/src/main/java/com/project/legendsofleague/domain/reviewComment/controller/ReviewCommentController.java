package com.project.legendsofleague.domain.reviewComment.controller;

import com.project.legendsofleague.domain.member.domain.CurrentMember;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.reviewComment.dto.ReviewCommentRequest;
import com.project.legendsofleague.domain.reviewComment.dto.ReviewCommentResponseDto;
import com.project.legendsofleague.domain.reviewComment.service.ReviewCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviewComment")
@Slf4j
public class ReviewCommentController {

    private final ReviewCommentService reviewCommentService;

    @PostMapping("/{gameId}")
    public String writeReviewComment(@CurrentMember Member member, @RequestBody ReviewCommentRequest reviewCommentRequest) {
        log.info("ReviewCommentController.writeReviewComment");
        log.info("넘어온 리뷰: 게임id={}, 리뷰={}", reviewCommentRequest.getGameId(), reviewCommentRequest.getComment());
        reviewCommentService.saveReviewComment(member, reviewCommentRequest);
        return "ok";
    }

    @GetMapping("/{gameId}")
    public ResponseEntity<List<ReviewCommentResponseDto>> getReviewComments(@PathVariable Long gameId) {
        log.info("ReviewCommentController.getReviewComments");
        List<ReviewCommentResponseDto> reviewComments = reviewCommentService.findReviewComments(gameId);
        return new ResponseEntity<>(reviewComments, HttpStatus.OK);
    }

}
