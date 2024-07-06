package com.project.legendsofleague.db.team.domain;

import com.project.legendsofleague.db.player.domain.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@NoArgsConstructor(access = PROTECTED)
public class Team {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "team_id")
    private Long id;

    private String teamName;
    private String uniqueLine;

    @OneToMany(mappedBy = "team")
    private List<Player> players = new ArrayList<>();

    public static Team toEntity(Map<String, String> teamData) {
        Team team = new Team();
        team.teamName = teamData.get("Team");
        team.uniqueLine = teamData.get("UniqueLine");
        return team;
    }
}
