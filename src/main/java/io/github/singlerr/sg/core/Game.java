package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import org.bukkit.event.Listener;

public interface Game {

  void initialize();

  void registerListener(Registry<Listener> registry);

  void registerGameListener(Registry<GameEventListener> registry);

  <T extends GameSettings> GameSetup<T> getGameSetup();

  default GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status) {
    return new GameContext(prev.getPlayers(), status, eventBus, null);
  }

}
