package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class GameSetupManager {

  private final GameRegistry games;
  private final Map<UUID, GameSetup> contexts;

  public GameSetupManager(GameRegistry games) {
    this.games = games;
    this.contexts = new HashMap<>();
  }

  public GameSetupContext<? extends GameSettings> getContext(UUID playerId) {
    return contexts.get(playerId).context();
  }

  public GameSetupContext<? extends GameSettings> createContext(String id, UUID playerId) {
    Game game = this.games.getById(id);
    if (game == null) {
      return null;
    }
    GameSetupContext<? extends GameSettings> context = game.getGameSetup().createContext();
    this.contexts.put(playerId, new GameSetup(game, context));
    return context;
  }

  public boolean isOnContext(UUID playerId) {
    return contexts.containsKey(playerId);
  }

  public boolean joinSetup(UUID playerId, String id) {
    if (isOnContext(playerId)) {
      return false;
    }

    Game game = this.games.getById(id);
    if (game == null) {
      return false;
    }

    GameSetupContext<? extends GameSettings> context = game.getGameSetup().createContext();
    this.contexts.put(playerId, new GameSetup(game, context));
    game.getGameSetup().setupStart((GameSetupContext<GameSettings>) context);

    return true;
  }

  public boolean exitSetup(UUID playerId) {
    if (!isOnContext(playerId)) {
      return false;
    }

    GameSetup game = this.contexts.get(playerId);
    game.game().getGameSetup().setupEnd((GameSetupContext<GameSettings>) game.context());
    return true;
  }

  private record GameSetup(Game game, GameSetupContext<? extends GameSettings> context) {
  }
}
