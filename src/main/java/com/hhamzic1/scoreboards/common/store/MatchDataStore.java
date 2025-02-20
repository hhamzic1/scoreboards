package com.hhamzic1.scoreboards.common.store;

import com.hhamzic1.scoreboards.common.model.Match;

import java.util.UUID;

public interface MatchDataStore {

    void save(UUID matchId, Match match);
}