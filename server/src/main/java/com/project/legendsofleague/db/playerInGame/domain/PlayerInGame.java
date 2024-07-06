package com.project.legendsofleague.db.playerInGame.domain;

import com.project.legendsofleague.db.player.domain.Player;
import com.project.legendsofleague.db.teamInGame.domain.TeamInGame;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class PlayerInGame {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "player_in_game_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "team_in_game_id")
    private TeamInGame teamInGame;

    public static PlayerInGame toEntity(Player player, TeamInGame teamInGame) {
        PlayerInGame playerInGame = new PlayerInGame();
        playerInGame.player = player;
        playerInGame.teamInGame = teamInGame;
        return playerInGame;
    }
}
