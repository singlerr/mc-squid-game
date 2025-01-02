package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.trolley.listener.TrolleyEventListener;
import java.util.HashMap;
import lombok.Getter;
import org.bukkit.event.Listener;

public final class TrolleyGame implements Game {
  @Getter
  private TrolleyGameContext context;
  @Getter
  private TrolleyGameSetup setup = new TrolleyGameSetup();

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {
    registry.register("trolley_event_listener", new TrolleyEventListener(this));
  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {
    registry.register("trolley_game_listener", new TrolleyGameEventListener(this));
  }

  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {
    return (GameSetup<T>) setup;
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (context =
        (prev != null ? new TrolleyGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new TrolleyGameContext(new HashMap<>(), status, eventBus, settings)));
  }
}
