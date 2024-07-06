package com.project.legendsofleague.db.player.domain;

import com.project.legendsofleague.db.team.domain.Team;
import com.project.legendsofleague.db.playerInGame.domain.PlayerInGame;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
public class Player {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "player_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(STRING)
    private Position position;

    private String playerName;

    @OneToMany(mappedBy = "player")
    private List<PlayerInGame> playedGames = new ArrayList<>();

    public void setTeam(Team team) {
        this.team = team;
    }

    public static Player toEntity(Map<String, String> players) {
        Player player = new Player();
        player.playerName = players.get("Player");
        player.position = Position.fromString(players.get("Role"));
        return player;
    }
}
