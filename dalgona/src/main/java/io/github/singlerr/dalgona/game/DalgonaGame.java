package io.github.singlerr.dalgona.game;

import io.github.singlerr.dalgona.listener.DalgonaEventListener;
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

public final class DalgonaGame implements Game {

  private DalgonaGameSetup setup = new DalgonaGameSetup();

  @Getter
  private DalgonaGameContext context;

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {
    registry.register("dalgona_listener", new DalgonaEventListener(this));
  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {
    registry.register("dalgona_game_listener", new DalgonaGameEventListener(this));
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (context =
        (prev != null ? new DalgonaGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new DalgonaGameContext(new HashMap<>(), status, eventBus, settings)));
  }

  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {
    return (GameSetup<T>) setup;
  }
}
