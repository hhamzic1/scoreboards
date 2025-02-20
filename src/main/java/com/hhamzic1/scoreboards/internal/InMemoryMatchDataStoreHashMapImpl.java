package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.common.model.Match;
import com.hhamzic1.scoreboards.common.store.InMemoryMatchDataStore;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryMatchDataStoreHashMapImpl implements InMemoryMatchDataStore {

    private final Map<UUID, Match> activeMatchesStore = new ConcurrentHashMap<>();

    @Override
    public void save(UUID matchId, Match match) {
        activeMatchesStore.putIfAbsent(matchId, match);
    }

    @Override
    public void clear() {
        activeMatchesStore.clear();
    }
}