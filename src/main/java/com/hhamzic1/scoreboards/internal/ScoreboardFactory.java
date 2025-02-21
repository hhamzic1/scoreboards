package com.hhamzic1.scoreboards.internal;

import com.hhamzic1.scoreboards.Scoreboard;
import com.hhamzic1.scoreboards.common.exception.UnsupportedSportTypeException;
import com.hhamzic1.scoreboards.common.model.SportType;
import com.hhamzic1.scoreboards.common.store.MatchDataStore;

public class ScoreboardFactory {

    public static Scoreboard createScoreboard(SportType sportType) {
        return createScoreboard(sportType, new InMemoryMatchDataStoreHashMapImpl());
    }

    public static Scoreboard createScoreboard(SportType sportType, MatchDataStore matchDataStore) {
        return switch (sportType) {
            case FOOTBALL -> new FootballScoreboardImpl(matchDataStore, new FootballScoreboardValidatorImpl());
            default -> throw new UnsupportedSportTypeException();
        };
    }
}