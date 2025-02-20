package com.hhamzic1.scoreboards.common.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Match(UUID id, OffsetDateTime startTime, OffsetDateTime endTime,
                    Team homeTeam, Team awayTeam, Score score) {

    public Match(Team homeTeam, Team awayTeam, Score score) {
        this(UUID.randomUUID(), OffsetDateTime.now(), null, homeTeam, awayTeam, score);
    }

    @Override
    public String toString() {
        return "%s %d - %s %d".formatted(homeTeam.name(), score.homeTeamScore(), awayTeam.name(), score.awayTeamScore());
    }
}