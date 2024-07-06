package com.project.legendsofleague.db.teamInGame.service;

import com.project.legendsofleague.db.game.domain.Game;
import com.project.legendsofleague.db.game.repository.GameRepository;
import com.project.legendsofleague.db.team.domain.Team;
import com.project.legendsofleague.db.team.repository.TeamRepository;
import com.project.legendsofleague.db.teamInGame.domain.Camp;
import com.project.legendsofleague.db.teamInGame.domain.TeamInGame;
import com.project.legendsofleague.db.teamInGame.repository.TeamInGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TeamInGameService {

    private final TeamInGameRepository teamInGameRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;

    @Transactional
    public void saveTeamInGame(List<Map<String, String>> matchSchedules) {

        for (Map<String, String> match : matchSchedules) {
            String uniqueGame = match.get("MatchId");
            Game game = gameRepository.findByUniqueGame(uniqueGame);

            String team1Score = match.get("Team1Score");
            Team team1 = teamRepository.findByTeamName(match.get("Team1"));

            TeamInGame blueTeam = TeamInGame.toEntity(team1Score, Camp.BLUE, uniqueGame);
            blueTeam.setTeamAndGame(team1, game);
            teamInGameRepository.save(blueTeam);

            String team2Score = match.get("Team2Score");
            Team team2 = teamRepository.findByTeamName(match.get("Team2"));

            TeamInGame redTeam = TeamInGame.toEntity(team2Score, Camp.RED, uniqueGame);
            redTeam.setTeamAndGame(team2, game);
            teamInGameRepository.save(redTeam);
        }
    }
}
