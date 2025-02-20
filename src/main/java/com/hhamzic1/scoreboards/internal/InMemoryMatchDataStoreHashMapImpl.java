package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.exception.MatchStoreException;
import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.common.store.InMemoryMatchDataStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.nonNull;

class InMemoryMatchDataStoreHashMapImpl implements InMemoryMatchDataStore {

    private final Map<UUID, Match> activeMatchesStore = new ConcurrentHashMap<>();

    @Override
    public void save(UUID matchId, Match match) {
        synchronized (this) {
            if (isAnyTeamInAnotherActiveMatch(match.homeTeam(), match.awayTeam())) {
                throw new MatchStoreException("Some of the teams are already in another ongoing match.");
            }

            activeMatchesStore.compute(matchId, (key, value) -> {
                if (nonNull(value)) {
                    throw new MatchStoreException("Match with ID '%s' already exists!".formatted(matchId));
                }

                return match;
            });
        }
    }

    @Override
    public void clear() {
        activeMatchesStore.clear();
    }

    private boolean isAnyTeamInAnotherActiveMatch(Team homeTeam, Team awayTeam) {
        return activeMatchesStore.values().stream()
                .anyMatch(activeMatch -> activeMatch.homeTeam().id().equals(homeTeam.id())
                        || activeMatch.awayTeam().id().equals(homeTeam.id())
                        || activeMatch.homeTeam().id().equals(awayTeam.id())
                        || activeMatch.awayTeam().id().equals(awayTeam.id())
                );
    }
}