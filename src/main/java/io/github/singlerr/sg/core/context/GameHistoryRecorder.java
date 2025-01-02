package io.github.singlerr.sg.core.context;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;

@UtilityClass
public class GameHistoryRecorder {

  private final Map<UUID, GameHistory> games = Collections.synchronizedMap(new HashMap<>());

  public void recordGame(UUID id, GameContext context) {
    games.put(id, new GameHistory(id.toString(), context.getInitialPlayerSize(),
        context.getPlayerMap().size()));
  }

  public Collection<GameHistory> getAll() {
    return games.values();
  }
}
