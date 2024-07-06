package com.project.legendsofleague.db.initDB;

import com.project.legendsofleague.db.game.service.GameService;
import com.project.legendsofleague.db.player.service.PlayerService;
import com.project.legendsofleague.db.playerInGame.service.PlayerInGameService;
import com.project.legendsofleague.db.team.service.TeamService;
import com.project.legendsofleague.db.teamInGame.service.TeamInGameService;
import com.project.legendsofleague.util.excel.ExcelReader;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

//@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final ExcelReader excelReader;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final GameService gameService;
    private final TeamInGameService teamInGameService;
    private final PlayerInGameService playerInGameService;

    @Value("${file.excel}")
    private String filePath;

    @PostConstruct
    public void initializeDatabase() throws Exception {
        log.info("initializeDatabase");

        List<Map<String, String>> teams = excelReader.readTeam(filePath);
        log.info("팀 목록={}", teams);
        teamService.saveTeam(teams);

        List<Map<String, String>> players = excelReader.readPlayer(filePath);
        log.info("선수 명단={}", players);
        playerService.savePlayer(players);

        List<Map<String, String>> matchSchedules = excelReader.readMatchSchedule(filePath);
        log.info("경기 일정={}", matchSchedules);
        gameService.saveGame(matchSchedules);
        teamInGameService.saveTeamInGame(matchSchedules);

        List<Map<String, String>> rostersByGames = excelReader.readRostersByGame(filePath);
        log.info("경기 출전 명단={}", rostersByGames);
        playerInGameService.saveRoster(rostersByGames);

    }
}
