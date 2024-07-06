package com.project.legendsofleague.domain.rate.service;

import com.project.legendsofleague.db.game.domain.Game;
import com.project.legendsofleague.db.game.repository.GameRepository;
import com.project.legendsofleague.db.playerInGame.domain.PlayerInGame;
import com.project.legendsofleague.db.playerInGame.repository.PlayerInGameRepository;
import com.project.legendsofleague.domain.member.domain.Member;
import com.project.legendsofleague.domain.rate.domain.Rate;
import com.project.legendsofleague.domain.rate.dto.RateDto;
import com.project.legendsofleague.domain.rate.dto.RateRequestDto;
import com.project.legendsofleague.domain.rate.dto.RateResponseDto;
import com.project.legendsofleague.domain.rate.repository.RateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class RateService {

    private final GameRepository gameRepository;
    private final PlayerInGameRepository playerInGameRepository;
    private final RateRepository rateRepository;

    @Transactional
    public void saveRate(Member member, RateRequestDto rateRequestDto, Long gameId) {

        Game game = gameRepository.findById(gameId).orElseThrow();

        for (RateDto playerScore : rateRequestDto.getPlayerScores()) {
            PlayerInGame playerInGame = playerInGameRepository.findById(playerScore.getPlayerId()).orElseThrow();
            Rate rate;
            if (playerScore.getScore().isEmpty()) {
                rate = Rate.toEntity(-1);
            } else {
                rate = Rate.toEntity(Integer.valueOf(playerScore.getScore()));
            }
            rate.setPlayerInGameAndMemberAndGame(playerInGame, member, game);
            rateRepository.save(rate);
        }
    }

    public List<RateResponseDto> findRates(Member member, Long gameId) {
        List<RateResponseDto> list = new ArrayList<>();
        Game game = gameRepository.findById(gameId).orElseThrow();
        List<Rate> rates = rateRepository.findAllByMemberAndGame(member, game);
        RateResponseDto rateResponseDto;

        if(!rates.isEmpty()) {
            for (Rate rate : rates) {
                log.info("게임ID={}", rate.getGame().getId());
                log.info("선수={}", rate.getPlayerInGame().getPlayer().getPlayerName());
                log.info("평점={}", rate.getScore());
                int sum = 0;
                PlayerInGame playerInGame = rate.getPlayerInGame();
                List<Rate> ratesForAverage = rateRepository.findAllByPlayerInGameAndScoreNot(playerInGame, -1);

                    for (Rate rateForAverage : ratesForAverage) {
                        sum += rateForAverage.getScore();
                    }

                    double average = (double) sum / ratesForAverage.size();
                    DecimalFormat df = new DecimalFormat("#.0");

                    String formattedDouble = df.format(average);
                    rateResponseDto = RateResponseDto.toDto(rate, formattedDouble);
                list.add(rateResponseDto);
            }
        }
        return list;
    }
}
