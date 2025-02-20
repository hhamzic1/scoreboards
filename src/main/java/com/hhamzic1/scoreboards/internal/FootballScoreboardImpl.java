package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.Scoreboard;
import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Score;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.common.store.MatchDataStore;

class FootballScoreboardImpl implements Scoreboard {

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

        matchDataStore.save(match.id(), match);

        return match;
    }
}