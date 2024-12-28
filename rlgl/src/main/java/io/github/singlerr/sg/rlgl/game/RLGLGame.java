package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import org.bukkit.event.Listener;

public final class RLGLGame implements Game {

  private final RLGLGameSetup setup = new RLGLGameSetup();


  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {

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
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status) {
    return Game.super.createContext(prev, eventBus, status);
  }
}
