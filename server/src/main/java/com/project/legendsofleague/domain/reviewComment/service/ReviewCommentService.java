package com.project.legendsofleague.domain.reviewComment.service;

import com.project.legendsofleague.db.game.domain.Game;
import com.project.legendsofleague.db.game.repository.GameRepository;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.reviewComment.domain.ReviewComment;
import com.project.legendsofleague.domain.reviewComment.dto.ReviewCommentRequest;
import com.project.legendsofleague.domain.reviewComment.dto.ReviewCommentResponseDto;
import com.project.legendsofleague.domain.reviewComment.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReviewCommentService {

    private final ReviewCommentRepository reviewCommentRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void saveReviewComment(Member member, ReviewCommentRequest reviewCommentRequest) {
        Game game = gameRepository.findById(reviewCommentRequest.getGameId()).orElseThrow();
        ReviewComment reviewComment = ReviewComment.toEntity(reviewCommentRequest);
        reviewComment.setMemberAndGame(member, game);
        reviewCommentRepository.save(reviewComment);
    }

    public List<ReviewCommentResponseDto> findReviewComments(Long gameId) {
        List<ReviewCommentResponseDto> reviewCommentResponseDtos = new ArrayList<>();
        List<ReviewComment> allByGameId = reviewCommentRepository.findAllByGameId(gameId);
        int size = allByGameId.size();
        if (size < 5) {
            List<ReviewComment> reviewComments = reviewCommentRepository.findByGameIdAndIsDeleteFalse(gameId).orElseThrow();
            for (ReviewComment reviewComment : reviewComments) {
                log.info("리뷰Id={}", reviewComment.getId());
                log.info("리뷰 작성자={}", reviewComment.getMember().getUsername());
                log.info("내용={}", reviewComment.getComment());
                ReviewCommentResponseDto dto = ReviewCommentResponseDto.toDto(reviewComment);
                reviewCommentResponseDtos.add(dto);
            }
            return reviewCommentResponseDtos;
        } else {
            Optional<ReviewComment> topReviewComment = reviewCommentRepository.findFirstByBuffCountGreaterThanEqualOrderByBuffCountDesc(5);
            if(topReviewComment.isEmpty()) {
                List<ReviewComment> reviewComments = reviewCommentRepository.findByGameIdAndIsDeleteFalse(gameId).orElseThrow();
                for (ReviewComment reviewComment : reviewComments) {
                    log.info("리뷰Id={}", reviewComment.getId());
                    log.info("리뷰 작성자={}", reviewComment.getMember().getUsername());
                    log.info("내용={}", reviewComment.getComment());
                    ReviewCommentResponseDto dto = ReviewCommentResponseDto.toDto(reviewComment);
                    reviewCommentResponseDtos.add(dto);
                }
                return reviewCommentResponseDtos;
            } else {
                ReviewCommentResponseDto topDto = ReviewCommentResponseDto.toDto(topReviewComment.get());
                reviewCommentResponseDtos.add(topDto);
                List<ReviewComment> reviewComments = reviewCommentRepository.findByGameIdAndIsDeleteFalse(gameId).orElseThrow();
                for (ReviewComment reviewComment : reviewComments) {
                    log.info("리뷰Id={}", reviewComment.getId());
                    log.info("리뷰 작성자={}", reviewComment.getMember().getUsername());
                    log.info("내용={}", reviewComment.getComment());
                    ReviewCommentResponseDto dto = ReviewCommentResponseDto.toDto(reviewComment);
                    reviewCommentResponseDtos.add(dto);
                }
                return reviewCommentResponseDtos;
            }
        }
    }
}
