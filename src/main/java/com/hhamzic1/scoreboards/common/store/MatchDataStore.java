package com.hhamzic1.scoreboards.common.store;

import com.hhamzic1.scoreboards.common.model.Match;

import java.util.List;
import java.util.UUID;

public interface MatchDataStore {

    void save(UUID matchId, Match match);

    void delete(UUID matchId);

    List<Match> getAllFinished();
}