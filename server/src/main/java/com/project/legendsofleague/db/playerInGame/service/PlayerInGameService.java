package com.project.legendsofleague.db.playerInGame.service;

import com.project.legendsofleague.db.player.domain.Player;
import com.project.legendsofleague.db.player.repository.PlayerRepository;
import com.project.legendsofleague.db.playerInGame.domain.PlayerInGame;
import com.project.legendsofleague.db.playerInGame.repository.PlayerInGameRepository;
import com.project.legendsofleague.db.team.domain.Team;
import com.project.legendsofleague.db.team.repository.TeamRepository;
import com.project.legendsofleague.db.teamInGame.domain.TeamInGame;
import com.project.legendsofleague.db.teamInGame.repository.TeamInGameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlayerInGameService {

    private final PlayerInGameRepository playerInGameRepository;
    private final TeamInGameRepository teamInGameRepository;
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public void saveRoster(List<Map<String, String>> rostersByGames) {

        String matchId = "";
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        if (!rostersByGames.isEmpty()) {
            Map<String, String> firstRoster = rostersByGames.get(0);
            matchId = firstRoster.get("MatchId");
            map.put("MatchId", matchId);
            map.put("Team1", firstRoster.get("Team1"));
            map.put("Team2", firstRoster.get("Team2"));
            String team1Players = firstRoster.get("Team1Players");
            LinkedHashSet<String> linkedHashSet1 = new LinkedHashSet<>(Arrays.asList(team1Players.split(",")));
            map.put("Team1Players", linkedHashSet1);
            String team2Players = firstRoster.get("Team2Players");
            LinkedHashSet<String> linkedHashSet2 = new LinkedHashSet<>(Arrays.asList(team2Players.split(",")));
            map.put("Team2Players", linkedHashSet2);
        }


        for (Map<String, String> rosters : rostersByGames) {
            if (rosters.get("MatchId").equals(matchId)) {
                if (!rosters.get("Team1").equals(map.get("Team1"))) {
                    LinkedHashSet<String> team1PlayersLhs = (LinkedHashSet<String>) map.get("Team1Players");
                    team1PlayersLhs.addAll(Arrays.asList(rosters.get("Team2Players").split(",")));
                    map.put("Team1Players", team1PlayersLhs);

                    LinkedHashSet<String> team2PlayersLhs = (LinkedHashSet<String>) map.get("Team2Players");
                    team2PlayersLhs.addAll(Arrays.asList(rosters.get("Team1Players").split(",")));
                    map.put("Team2Players", team2PlayersLhs);
                } else {
                    LinkedHashSet<String> team1PlayersLhs = (LinkedHashSet<String>) map.get("Team1Players");
                    String team1Players = rosters.get("Team1Players");
                    team1PlayersLhs.addAll(Arrays.asList(team1Players.split(",")));
                    map.put("Team1Players", team1PlayersLhs);

                    LinkedHashSet<String> team2PlayersLhs = (LinkedHashSet<String>) map.get("Team2Players");
                    String team2Players = rosters.get("Team2Players");
                    team2PlayersLhs.addAll(Arrays.asList(team2Players.split(",")));
                    map.put("Team2Players", team2PlayersLhs);
                }
            } else {

                Team team1 = teamRepository.findByTeamName(String.valueOf(map.get("Team1")));
                TeamInGame team1InGame = teamInGameRepository.findByUniqueGameAndTeam(matchId, team1);
                LinkedHashSet<String> team1Roster = (LinkedHashSet<String>) map.get("Team1Players");
                for (String playerName : team1Roster) {
                    Player player = playerRepository.findByPlayerName(playerName);
                    PlayerInGame entity = PlayerInGame.toEntity(player, team1InGame);
                    playerInGameRepository.save(entity);
                }

                Team team2 = teamRepository.findByTeamName(String.valueOf(map.get("Team2")));
                TeamInGame team2InGame = teamInGameRepository.findByUniqueGameAndTeam(matchId, team2);
                LinkedHashSet<String> team2Roster = (LinkedHashSet<String>) map.get("Team2Players");
                for (String playerName : team2Roster) {
                    Player player = playerRepository.findByPlayerName(playerName);
                    PlayerInGame entity = PlayerInGame.toEntity(player, team2InGame);
                    playerInGameRepository.save(entity);
                }

                matchId = rosters.get("MatchId");
                map.put("MatchId", matchId);
                map.put("Team1", rosters.get("Team1"));
                map.put("Team2", rosters.get("Team2"));
                String team1Players = rosters.get("Team1Players");
                LinkedHashSet<String> linkedHashSet1 = new LinkedHashSet<>(Arrays.asList(team1Players.split(",")));
                map.put("Team1Players", linkedHashSet1);
                String team2Players = rosters.get("Team2Players");
                LinkedHashSet<String> linkedHashSet2 = new LinkedHashSet<>(Arrays.asList(team2Players.split(",")));
                map.put("Team2Players", linkedHashSet2);
            }
        }
    }
}
