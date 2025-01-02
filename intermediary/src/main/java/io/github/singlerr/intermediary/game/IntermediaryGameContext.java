package io.github.singlerr.intermediary.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.Map;
import java.util.UUID;

public final class IntermediaryGameContext extends GameContext {
  public IntermediaryGameContext(
      Map<UUID, GamePlayer> players,
      GameStatus status,
      GameEventBus eventBus,
      GameSettings settings) {
    super(players, status, eventBus, settings);
  }
}
