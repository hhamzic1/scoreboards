# Coding exercise (Live Odds Service)

### Task requirements:

You are working in a sports data company, and we would like you to develop a new Live Football
World Cup Scoreboard library that shows all the ongoing matches and their scores. 

**The scoreboard supports the following operations:** 

1. **Start a new match**, assuming initial score **0 – 0** and adding it to the scoreboard. This should capture the following parameters:
   - **Home team**
   - **Away team**

2. **Update score**. This should receive a pair of absolute scores:
   - **Home team score**
   - **Away team score**

3. **Finish match** currently in progress. This removes a match from the scoreboard.

4. **Get a summary** of matches in progress ordered by their total score. The matches with the same total score will be returned ordered by the most recently started match in the scoreboard.

**For example, if following matches are started in the specified order and their scores respectively updated:**

<pre>
  a. Mexico 0 - Canada 5 
  b. Spain 10 - Brazil 2 
  c. Germany 2 - France 2 
  d. Uruguay 6 - Italy 6 
  e. Argentina 3 - Australia 1
</pre>

**The summary should be as follows:**

<pre>
  1. Uruguay 6 - Italy 6
  2. Spain 10 - Brazil 2
  3. Mexico 0 - Canada 5
  4. Argentina 3 - Australia 1
  5. Germany 2 - France 2
</pre>

# Solution (way of thinking)

At first, this seems like a simple task but after a while you start to realize how deep the rabbit hole can go. After reading the requirements it's obvious that we need to develop a library and just that carries its own burden. The library is intended to be used by some end clients and we don't want to expose too much stuff to them, but still we want it to be as configurable as it gets.

We want this library to be as generic as possible. Why just to create a library that will work for football as a sport, when it could easily support other sports if needed?

So the first abstraction that we created was a sport one. The face/entry point of the library is a factory class called `ScoreboardFactory`. It's publicly available and it is used to create a scoreboard. The method that is used to instantiate a scoreboard instance is `ScoreboardFactory.createScoreboard(SportType sportType)`. It accepts `SportType` object and based on that it will return the right implementation of the scoreboard for a certain sport.

Now obivously per task requirements we need to store the scoreboard data somewhere. As proposed in the task itself for the sake of simplicity we can use a in-memory solution, but why not leave a possibility for an end client to define it's own data store implementation?

There is also `ScoreboardFactory.createScoreboard(SportType sportType, MatchDataStore matchDataStore)` method which accepts `MatchDataStore` interface that we defined.

`MatchDataStore` interface contains couple of methods that are needed for some store to be able to fulill the requirements of the task. We also introduced `InMemoryMatchDataStore` interface that extends `MatchDataStore` one and introduces couple of new methods that are common just for in-memory store solutions. `DbMatchDataStore` interface can be defined too (but we skipped it in the solution) and with this we achieve a good level of granularity, meaning we can pass different implementations of match data store, both in-memory and DB ones e.g. `InMemoryMatchDataStoreRedisImpl`, `InMemoryMatchDataStoreHazelcastImpl`, `DbMatchDataStorePostgresImpl`, `DbMatchDataStoreMSSQLImpl` etc... 
With this now in place, the end client can define it's own match data store and pass it to the factory in order to create a scoreboard for some sport that will use the defined store implementation.

Obviously the return value from the factory is a `Scoreboard` interface that has methods required in the task and it looks like this:

<pre>
  public interface Scoreboard {

    Match startMatch(Team homeTeam, Team awayTeam);

    void finishMatch(UUID matchId);

    Match updateScore(UUID matchId, Score score);

    List<Match> getActiveMatchesSummary();

    List<Match> getAllFinishedMatches();
}
</pre>

As it can be seen it has methods required in the task (`startMatch`, `finishMatch`, `updateScore` and `getActiveMatchesSummary`), but it would be great to be able to track finished/played matches also, hence the `getAllFinishedMatches` method.

For the purpose of the task we defined `FootballScoreboardImpl` that is a `package-private` implementation of the above mentioned interface (not visible to the end client). `FootballScoreboardImpl` and all future implementatios (e.g. `BasketballScoreboardImpl`) has a `MatchDataStore` instance as a property and a `ScoreboardValidator`.

`ScoreboardValidator` is also a `package-private` interface that defines validation methods: `validateOnStartMatch`, `validateOnFinishMatch`, `validateOnScoreUpdate`. In our case we have a `package-private` implementation of this interface called `FootballScoreboardValidatorImpl` that overrides the above mentioned methods to satisfy some football specific rules (e.g. we can't have negative score etc...). With this we left a space to define other validators that will be suitable for some other sports (e.g. `BasketballScoreboardValidatorImpl`).

Last but not least, there is also a custom comparator defined in `FootballScoreboardImpl` that is being used to sort the matches in some fashion (the one requested by the task). Some other implementations of the `Scoreboard` interface can have different comparators.

Regarding the objects used for data transfer, we need them to be immutable. We don't want changes made on e.g. `Match` object by the end user to be reflected in the store and create some inconsistencies. That's why we are using `java records` for `Match`, `Team`, `Score` objects. Those records are publicly available and the client/end user can instantiate them.

For the purpose of the task we implemented a `package-private` `InMemoryMatchDataStoreHashMapImpl` that uses `ConcurrentHashMap` under the hood for the storage. Because our store must be `thread-safe` and our operations must be atomic we use `ConcurrentHashMap`'s `compute` method all over the place. It handles `key-locking` and `thread-safety` for us out-of-the-box. But for `save` that isn't enough because we need to check if the teams that should start the match are already in some ongoing match. There we used `synchronized` block to make the `save` operation atomic and `thread-safe` in full (for the sake of simplicity we took this solution, of course this could be handled better, maybe even without full map lock and we are aware of it but we took this tradeoff - `update` statistically should be used more than `save`, that was the reasoning). 

As mentioned earlier we have another `ConcurrentHashMap` just for finished matches, and with that we have also a history of played matches. The value of the maps is a `package-private` `StoredMatch` record. Why do we have it? Well in order to guarantee the sort order of the matches from the tasks requirement (having matches with the same total score, the one that was started last should come first) relying just on start time (`OffsetDateTime`) isn't enough because two matches could have exactly the same start time (even to the nanoseconds) and the sort order wouldn't be consistent. To guarantee the order we somehow need to track when something entered in the store. Because `ConcurrentHashMap` doesn't provide that out-of-the-box, we created `StoredMatch` record that has `Long storeOrderId` property which is unique. On `save` (because it's atomic and `thread-safe`) when inserting `StoredMatch` we use `private static long storeOrderIdCounter` to determine the `storeOrderId`. Now two entires with same start time won't have same `storeOrderId` (the greater the `storeOrderId` is the entry is newer).
As for the tests, we tried to cover both normal and edge cases, even multithreading ones. TDD development was used.

### Conclusion

**We developed a highly generic library (both in terms of sports and match data storage) that is easily extendable and adaptable for future needs. It enforces controlled access, ensuring that end users interact only with the intended components while effectively encapsulating internal implementations. The default internal match data store guarantees atomicity and thread safety, while immutability safeguards it against any unwantedend users interactions.**
