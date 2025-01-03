package io.github.singlerr.intermediary.game;

import io.github.singlerr.intermediary.listener.IntermediaryEventListener;
import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.HashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.event.Listener;

@Slf4j
public final class IntermediaryGame implements Game {

  private final IntermediaryGameSetup setup = new IntermediaryGameSetup();
  @Getter
  private IntermediaryGameContext context;

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {
    registry.register("intermediary_listener", new IntermediaryEventListener(this));
  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {
    log.debug("Registered game event listener");
    registry.register("intermediary_game_listener", new IntermediaryGameEventListener());
  }

  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {
    return (GameSetup<T>) setup;
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (context =
        prev != null ?
            new IntermediaryGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new IntermediaryGameContext(new HashMap<>(), status, eventBus, settings));
  }
}
