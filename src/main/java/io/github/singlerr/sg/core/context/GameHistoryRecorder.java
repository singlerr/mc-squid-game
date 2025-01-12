package io.github.singlerr.sg.core.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GameHistoryRecorder {

  private final Map<String, GameHistory> games = Collections.synchronizedMap(new HashMap<>());

  public void recordGame(String id, GameContext context) {
    games.put(id, new GameHistory(id, context.getInitialPlayerSize(),
        context.getPlayers(GameRole.TROY.getLevel()).size()));
  }

  public Collection<GameHistory> getAll() {
    return games.values();
  }
}
