package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.model.Team;

interface ScoreboardValidator {

    void validateOnStartMatch(Team homeTeam, Team awayTeam);
}