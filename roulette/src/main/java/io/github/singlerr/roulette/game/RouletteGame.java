package io.github.singlerr.roulette.game;

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

public final class RouletteGame implements Game {

  @Getter
  private RouletteGameContext context;

  @Override
  public void initialize() {

  }

  @Override
  public void registerListener(Registry<Listener> registry) {

  }

  @Override
  public void registerGameListener(Registry<GameEventListener> registry) {

  }

  @Override
  public <T extends GameSettings> GameSetup<T> getGameSetup() {

    return null;
  }

  @Override
  public GameContext createContext(GameContext prev, GameEventBus eventBus, GameStatus status,
                                   GameSettings settings) {
    return (context =
        (prev != null ? new RouletteGameContext(prev.getPlayerMap(), status, eventBus, settings) :
            new RouletteGameContext(new HashMap<>(), status, eventBus, settings)));
  }
}
