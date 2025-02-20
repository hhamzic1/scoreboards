package com.hhamzic1.scoreboards;

import com.hhamzic1.scoreboards.common.SportType;
import com.hhamzic1.scoreboards.common.exception.MatchStoreException;
import com.hhamzic1.scoreboards.common.exception.ScoreboardException;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.internal.ScoreboardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void givenTeamAlreadyPlaying_whenStartingNewMatch_thenThrow() {
        var spain = new Team("Spain");
        var france = new Team("France");
        var italy = new Team("Italy");

        scoreboard.startMatch(spain, france);

        assertThrows(MatchStoreException.class, () -> scoreboard.startMatch(france, italy));
        assertThrows(MatchStoreException.class, () -> scoreboard.startMatch(italy, france));
        assertThrows(MatchStoreException.class, () -> scoreboard.startMatch(spain, italy));
        assertThrows(MatchStoreException.class, () -> scoreboard.startMatch(italy, spain));
    }

    @Test
    public void givenTeamAlreadyPlaying_whenStartingNewMatchWithMultipleThreads_thenThrow() throws InterruptedException {
        var spain = new Team("Spain");
        var france = new Team("France");
        var italy = new Team("Italy");
        var matchPairs = new Team[][]{
                {spain, france},
                {france, spain},
                {france, italy},
                {italy, france},
                {italy, spain},
                {spain, italy}
        };

        var executor = Executors.newFixedThreadPool(matchPairs.length);
        var latch = new CountDownLatch(matchPairs.length);
        var matchesStarted = new AtomicInteger(0);
        var matchesNotStarted = new AtomicInteger(0);

        for (var pair : matchPairs) {
            executor.submit(createMatchTask(scoreboard, pair[0], pair[1], matchesStarted, matchesNotStarted, latch));
        }

        latch.await();

        assertEquals(1, matchesStarted.get());
        assertEquals(5, matchesNotStarted.get());

        executor.shutdown();
    }

    private static Runnable createMatchTask(Scoreboard scoreboard, Team team1, Team team2,
                                            AtomicInteger matchesStarted, AtomicInteger matchesNotStarted, CountDownLatch latch) {
        return () -> {
            try {
                scoreboard.startMatch(team1, team2);
                matchesStarted.incrementAndGet();
            } catch (Exception e) {
                if (e instanceof MatchStoreException) {
                    matchesNotStarted.incrementAndGet();
                }
            } finally {
                latch.countDown();
            }
        };
    }
}