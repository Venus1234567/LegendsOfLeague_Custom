package com.project.legendsofleague.domain.buff.service;

import com.project.legendsofleague.domain.buff.domain.Buff;
import com.project.legendsofleague.domain.buff.dto.BuffRequestDto;
import com.project.legendsofleague.domain.buff.repository.BuffRepository;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.reviewComment.domain.ReviewComment;
import com.project.legendsofleague.domain.reviewComment.repository.ReviewCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuffService {

    private final BuffRepository buffRepository;
    private final ReviewCommentRepository  reviewCommentRepository;

    @Transactional
    public void saveBuff(Member member, BuffRequestDto buffRequestDto) {
        ReviewComment reviewComment = reviewCommentRepository.findById(buffRequestDto.getReviewCommentId()).orElseThrow();

        Optional<Buff> findBuff = buffRepository.findByMemberAndReviewComment(member, reviewComment);
        if (findBuff.isEmpty()) {
            Buff buff = Buff.toEntity(reviewComment, member);
            Buff savedBuff = buffRepository.save(buff);
            if (savedBuff.getId() > 0) {
                reviewComment.updateBuffCount(1);
            }
        } else {
            buffRepository.delete(findBuff.get());
            reviewComment.updateBuffCount(-1);
        }
    }


}
