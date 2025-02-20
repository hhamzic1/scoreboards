package com.hhamzic1.scoreboards.common.store;

import com.hhamzic1.scoreboards.common.model.Match;

import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface MatchDataStore {

    Match save(UUID matchId, Match match);

    Match update(UUID matchId, UnaryOperator<Match> updater);

    void delete(UUID matchId);

    List<Match> getAllFinished();
}