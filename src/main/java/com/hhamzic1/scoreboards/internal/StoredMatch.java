package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Score;
import com.hhamzic1.scoreboards.common.model.Team;

import java.time.OffsetDateTime;
import java.util.UUID;

record StoredMatch(UUID id, OffsetDateTime startTime, OffsetDateTime endTime, Team homeTeam, Team awayTeam,
                   Score score, Long storeOrderId) {

    StoredMatch(Match match, Long storeOrderId) {
        this(match.id(), match.startTime(), match.endTime(), match.homeTeam(), match.awayTeam(), match.score(), storeOrderId);
    }

    StoredMatch(StoredMatch storedMatch, OffsetDateTime endTime) {
        this(storedMatch.id(), storedMatch.startTime(), endTime, storedMatch.homeTeam(), storedMatch.awayTeam(), storedMatch.score(), storedMatch.storeOrderId());
    }

    Match toMatch() {
        return new Match(id, startTime, endTime, homeTeam, awayTeam, score);
    }
}