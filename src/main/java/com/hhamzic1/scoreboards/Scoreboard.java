package com.hhamzic1.scoreboards;

import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Score;
import com.hhamzic1.scoreboards.common.model.Team;

import java.util.List;
import java.util.UUID;

public interface Scoreboard {

    Match startMatch(Team homeTeam, Team awayTeam);

    void finishMatch(UUID matchId);

    Match updateScore(UUID matchId, Score score);

    List<Match> getAllFinishedMatches();
}