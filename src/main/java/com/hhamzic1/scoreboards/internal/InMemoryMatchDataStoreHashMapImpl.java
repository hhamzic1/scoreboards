package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.exception.MatchStoreException;
import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.model.Team;
import com.hhamzic1.scoreboards.common.store.InMemoryMatchDataStore;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

class InMemoryMatchDataStoreHashMapImpl implements InMemoryMatchDataStore {

    private final Map<UUID, StoredMatch> activeMatchesStore = new ConcurrentHashMap<>();
    private final Map<UUID, StoredMatch> finishedMatchesStore = new ConcurrentHashMap<>();
    private static long storeOrderIdCounter = 0;

    @Override
    public Match save(UUID matchId, Match match) {
        synchronized (this) {
            if (isAnyTeamInAnotherActiveMatch(match.homeTeam(), match.awayTeam())) {
                throw new MatchStoreException("Some of the teams are already in another ongoing match.");
            }

            return activeMatchesStore.compute(matchId, (key, value) -> {
                if (nonNull(value)) {
                    throw new MatchStoreException("Match with ID '%s' already exists!".formatted(matchId));
                }

                return new StoredMatch(match, ++storeOrderIdCounter);
            }).toMatch();
        }
    }

    @Override
    public Match update(UUID matchId, UnaryOperator<Match> updater) {
        return activeMatchesStore.compute(matchId, (key, value) -> {
            if (isNull(value)) {
                throw new MatchStoreException("Match with ID '%s' does not exist!".formatted(matchId));
            }

            return new StoredMatch(updater.apply(value.toMatch()), value.storeOrderId());
        }).toMatch();
    }

    @Override
    public void delete(UUID matchId) {
        activeMatchesStore.compute(matchId, (key, value) -> {
            if (isNull(value)) {
                throw new MatchStoreException("Match with ID '%s' doesn't exist!".formatted(matchId));
            }

            finishedMatchesStore.putIfAbsent(matchId, new StoredMatch(value, OffsetDateTime.now()));

            return null;
        });
    }

    @Override
    public List<Match> getAllActive() {
        return activeMatchesStore.values().stream()
                .map(StoredMatch::toMatch)
                .toList();
    }

    @Override
    public List<Match> getAllActive(Comparator<?> comparator) {
        return activeMatchesStore.values().stream()
                .sorted((Comparator<StoredMatch>) comparator)
                .map(StoredMatch::toMatch)
                .toList();
    }

    @Override
    public List<Match> getAllFinished() {
        return finishedMatchesStore.values().stream()
                .map(StoredMatch::toMatch)
                .toList();
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