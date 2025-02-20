package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.exception.ScoreboardException;
import com.hhamzic1.scoreboards.common.model.Team;

import java.util.UUID;

import static java.util.Objects.isNull;

class FootballScoreboardValidatorImpl implements ScoreboardValidator {

    @Override
    public void validateOnStartMatch(Team homeTeam, Team awayTeam) {
        if (isNull(homeTeam) || isNull(awayTeam)) {
            throw new ScoreboardException("Team as a parameter can't be null!");
        }

        if (homeTeam.id().equals(awayTeam.id())) {
            throw new ScoreboardException("A team can't play against itself!");
        }
    }

    @Override
    public void validateOnFinishMatch(UUID matchId) {
        if (isNull(matchId)) {
            throw new ScoreboardException("Match ID can't be null");
        }
    }
}