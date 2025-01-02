package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.rlgl.listener.RLGLEventListener;
import java.util.HashMap;
import lombok.Getter;
import org.bukkit.event.Listener;

public final class RLGLGame implements Game {

  private final RLGLGameSetup setup = new RLGLGameSetup();
  @Getter
  private RLGLGameContext gameContext;

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {
    registry.register("rlgl_listener", new RLGLEventListener(this));
  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {
    registry.register("rlgl_game_listener", new RLGLGameEventListener());
  }


  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {
    return (GameSetup<T>) setup;
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (gameContext =
        (prev != null ? new RLGLGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new RLGLGameContext(new HashMap<>(), status, eventBus, settings)));
  }
}
