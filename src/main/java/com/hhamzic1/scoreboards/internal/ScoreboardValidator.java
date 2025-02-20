package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.model.Team;

import java.util.UUID;

interface ScoreboardValidator {

    void validateOnStartMatch(Team homeTeam, Team awayTeam);

    void validateOnFinishMatch(UUID matchId);
}