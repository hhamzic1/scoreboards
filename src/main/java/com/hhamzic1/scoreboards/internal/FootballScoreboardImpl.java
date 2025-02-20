package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.Scoreboard;
import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Score;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.common.store.MatchDataStore;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

class FootballScoreboardImpl implements Scoreboard {

    private static final Comparator<StoredMatch> ACTIVE_FOOTBALL_MATCHES_SUMMARY_COMPARATOR =
            Comparator.<StoredMatch>comparingLong(match -> match.score().homeTeamScore() + match.score().awayTeamScore())
                    .reversed()
                    .thenComparing(StoredMatch::startTime, Comparator.reverseOrder())
                    .thenComparing(StoredMatch::storeOrderId, Comparator.reverseOrder());
    private final MatchDataStore matchDataStore;
    private final ScoreboardValidator validator;

    FootballScoreboardImpl(MatchDataStore matchDataStore, ScoreboardValidator validator) {
        this.matchDataStore = matchDataStore;
        this.validator = validator;
    }

    @Override
    public Match startMatch(Team homeTeam, Team awayTeam) {
        validator.validateOnStartMatch(homeTeam, awayTeam);

        var initialScore = new Score(0, 0);
        var match = new Match(homeTeam, awayTeam, initialScore);

        return matchDataStore.save(match.id(), match);
    }

    @Override
    public void finishMatch(UUID matchId) {
        validator.validateOnFinishMatch(matchId);

        matchDataStore.delete(matchId);
    }

    @Override
    public Match updateScore(UUID matchId, Score score) {
        validator.validateOnScoreUpdate(matchId, score);

        return matchDataStore.update(matchId, match -> new Match(match, score));
    }

    @Override
    public List<Match> getActiveMatchesSummary() {
        return matchDataStore.getAllActive(ACTIVE_FOOTBALL_MATCHES_SUMMARY_COMPARATOR);
    }

    @Override
    public List<Match> getAllFinishedMatches() {
        return matchDataStore.getAllFinished();
    }
}