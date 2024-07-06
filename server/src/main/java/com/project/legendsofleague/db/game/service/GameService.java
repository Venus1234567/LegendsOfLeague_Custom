package com.project.legendsofleague.db.game.service;

import com.project.legendsofleague.db.game.domain.Game;
import com.project.legendsofleague.db.game.dto.GameResultResponseDto;
import com.project.legendsofleague.db.game.dto.GameScheduleResponseDto;
import com.project.legendsofleague.db.game.dto.PlayerInGameDto;
import com.project.legendsofleague.db.game.dto.TeamInGameDto;
import com.project.legendsofleague.db.game.repository.GameRepository;
import com.project.legendsofleague.db.playerInGame.domain.PlayerInGame;
import com.project.legendsofleague.db.teamInGame.domain.TeamInGame;
import com.project.legendsofleague.db.teamInGame.repository.TeamInGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.project.legendsofleague.db.teamInGame.domain.Camp.BLUE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GameService {

    private final GameRepository gameRepository;
    private final TeamInGameRepository teamInGameRepository;

    @Transactional
    public void saveGame(List<Map<String, String>> matchSchedules) {

        for (Map<String, String> match : matchSchedules) {
            String dateTimeUtc = match.get("DateTime_UTC");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeUtc, formatter);

            Game entity = Game.toEntity(match);
            entity.setDateTime(localDateTime);
            gameRepository.save(entity);
        }
    }

    public List<GameScheduleResponseDto> getGames() {
        List<GameScheduleResponseDto> list = new ArrayList<>();
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            GameScheduleResponseDto gameScheduleResponseDto = GameScheduleResponseDto.toDto(game);
            for (TeamInGame team : game.getGames()) {
                TeamInGameDto teamInGameDto = TeamInGameDto.toDto(team);
                if (team.getCamp() == BLUE) {
                    gameScheduleResponseDto.setBlue(teamInGameDto);
                } else {
                    gameScheduleResponseDto.setRed(teamInGameDto);
                }
            }
            list.add(gameScheduleResponseDto);
        }
        return list;
    }

    public GameResultResponseDto getTeamsAndRostersInGame(Long gameId) {
        List<TeamInGame> result = teamInGameRepository.findAllByGameId(gameId);
        GameResultResponseDto gameResultResponseDto = GameResultResponseDto.toDto(gameId);
        for (TeamInGame teamInGame : result) {
            TeamInGameDto dto = TeamInGameDto.toDto(teamInGame);
            for (PlayerInGame player : teamInGame.getRosters()) {
                PlayerInGameDto playerDto = PlayerInGameDto.toDto(player);
                dto.setRosters(playerDto);
            }
            if (teamInGame.getCamp() == BLUE) {
                gameResultResponseDto.setBlue(dto);
            } else {
                gameResultResponseDto.setRed(dto);
            }
        }

        return gameResultResponseDto;
    }
}
