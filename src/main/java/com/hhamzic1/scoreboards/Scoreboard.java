package com.hhamzic1.scoreboards;

import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Team;

public interface Scoreboard {

    Match startMatch(Team homeTeam, Team awayTeam);
}