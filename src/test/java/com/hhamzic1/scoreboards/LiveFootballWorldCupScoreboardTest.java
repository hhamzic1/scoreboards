package com.hhamzic1.scoreboards;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LiveFootballWorldCupScoreboardTest {

    private Scoreboard scoreboard;

    @BeforeEach
    public void setup() {
        scoreboard = ScoreboardFactory.createScoreboard(SportType.FOOTBALL);
    }

    @Test
    public void givenDifferentTeams_whenStartingMatch_thenStartMatchSuccessfully() {
        var spain = new Team("Spain");
        var france = new Team("France");

        var match = scoreboard.startMatch(spain, france);

        assertEquals(match.homeTeam().id(), spain.id());
        assertEquals(match.awayTeam().id(), france.id());
        assertEquals(0, match.score().homeTeamScore());
        assertEquals(0, match.score().awayTeamScore());
    }

    @Test
    public void givenNotValidTeams_whenStartingMatch_thenThrow() {
        var spain = new Team("Spain");

        assertThrows(ScoreboardException.class, () -> scoreboard.startMatch(spain, null));
        assertThrows(ScoreboardException.class, () -> scoreboard.startMatch(null, spain));
        assertThrows(ScoreboardException.class, () -> scoreboard.startMatch(spain, spain));
    }
}