package io.github.singlerr.mgr.game;

import io.github.singlerr.mgr.listener.MGREventListener;
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
import org.bukkit.event.Listener;

public final class MGRGame implements Game {

  private MGRGameSetup setup = new MGRGameSetup();
  @Getter
  private MGRGameContext context;

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {
    registry.register("mgr_listener", new MGREventListener(this));
  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {
    registry.register("mgr_game_listener", new MGRGameEventListener(this, setup));
  }

  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {
    return (GameSetup<T>) setup;
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (context =
        prev != null ? new MGRGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new MGRGameContext(new HashMap<>(), status, eventBus, settings));
  }
}
