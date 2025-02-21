package com.hhamzic1.scoreboards;

import com.hhamzic1.scoreboards.common.exception.MatchStoreException;
import com.hhamzic1.scoreboards.common.exception.ScoreboardException;
import com.hhamzic1.scoreboards.common.model.Score;
import com.hhamzic1.scoreboards.common.model.SportType;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.internal.ScoreboardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void givenAlreadyStartedMatch_whenFinishMatchCalled_thenFinishMatchSuccessfully() {
        var italy = new Team("Italy");
        var england = new Team("England");
        var match = scoreboard.startMatch(italy, england);

        scoreboard.finishMatch(match.id());

        var finishedMatch = scoreboard.getAllFinishedMatches().getFirst();

        assertEquals(match.id(), finishedMatch.id());
        assertNotNull(finishedMatch.endTime());
    }

    @Test
    public void givenAlreadyFinishedMatch_whenFinishMatchCalled_thenThrow() {
        var germany = new Team("Germany");
        var argentina = new Team("Argentina");
        var match = scoreboard.startMatch(germany, argentina);

        scoreboard.finishMatch(match.id());

        assertThrows(MatchStoreException.class, () -> scoreboard.finishMatch(match.id()));
        assertThrows(MatchStoreException.class, () -> scoreboard.finishMatch(UUID.randomUUID()));
        assertThrows(ScoreboardException.class, () -> scoreboard.finishMatch(null));
    }

    @Test
    public void givenActiveMatch_whenUpdateScoreCalled_updateScoreSuccessfully() {
        var germany = new Team("Germany");
        var argentina = new Team("Argentina");
        var match = scoreboard.startMatch(germany, argentina);

        var updatedMatch = scoreboard.updateScore(match.id(), new Score(3, 1));

        assertEquals(match.id(), updatedMatch.id());
        assertEquals(0, match.score().homeTeamScore());
        assertEquals(0, match.score().awayTeamScore());
        assertEquals(3, updatedMatch.score().homeTeamScore());
        assertEquals(1, updatedMatch.score().awayTeamScore());
    }

    @Test
    public void givenInvalidParameters_whenUpdateScoreCalled_thenThrow() {
        assertThrows(ScoreboardException.class, () -> scoreboard.updateScore(null, new Score(1, 0)));
        assertThrows(ScoreboardException.class, () -> scoreboard.updateScore(UUID.randomUUID(), null));
        assertThrows(ScoreboardException.class, () -> scoreboard.updateScore(UUID.randomUUID(), new Score(-1, 1)));
        assertThrows(ScoreboardException.class, () -> scoreboard.updateScore(UUID.randomUUID(), new Score(2, -1)));
    }

    @Test
    public void givenAlreadyFinishedMatch_whenUpdateScoreCalled_thenThrow() {
        var spain = new Team("Spain");
        var honduras = new Team("Honduras");
        var match = scoreboard.startMatch(spain, honduras);

        scoreboard.finishMatch(match.id());

        assertThrows(MatchStoreException.class, () -> scoreboard.updateScore(match.id(), new Score(1, 1)));
    }

    @Test
    public void givenActiveMatches_whenGetActiveMatchesSummaryCalled_thenReturnSortedActiveMatches() {
        var mexico = new Team("Mexico");
        var canada = new Team("Canada");
        var spain = new Team("Spain");
        var brazil = new Team("Brazil");
        var germany = new Team("Germany");
        var france = new Team("France");
        var uruguay = new Team("Uruguay");
        var italy = new Team("Italy");
        var argentina = new Team("Argentina");
        var australia = new Team("Australia");

        var match1 = scoreboard.startMatch(mexico, canada);
        var match2 = scoreboard.startMatch(spain, brazil);
        var match3 = scoreboard.startMatch(germany, france);
        var match4 = scoreboard.startMatch(uruguay, italy);
        var match5 = scoreboard.startMatch(argentina, australia);

        scoreboard.updateScore(match1.id(), new Score(0, 5));
        scoreboard.updateScore(match2.id(), new Score(10, 2));
        scoreboard.updateScore(match3.id(), new Score(2, 2));
        scoreboard.updateScore(match4.id(), new Score(6, 6));
        scoreboard.updateScore(match5.id(), new Score(3, 1));

        var activeMatchesSummary = scoreboard.getActiveMatchesSummary();

        assertEquals(5, activeMatchesSummary.size());
        assertEquals(match4.id(), activeMatchesSummary.get(0).id());
        assertEquals(match2.id(), activeMatchesSummary.get(1).id());
        assertEquals(match1.id(), activeMatchesSummary.get(2).id());
        assertEquals(match5.id(), activeMatchesSummary.get(3).id());
        assertEquals(match3.id(), activeMatchesSummary.get(4).id());
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