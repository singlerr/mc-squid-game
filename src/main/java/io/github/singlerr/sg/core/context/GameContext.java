package io.github.singlerr.sg.core.context;

import com.google.common.collect.Lists;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GameContext {

  private final List<GamePlayer> players;
  @Getter(value = AccessLevel.NONE)
  private final GameEventBus eventBus;
  private final GameSettings settings;
  @Setter
  private GameStatus status;

  public GameContext(List<GamePlayer> players, GameStatus status, GameEventBus eventBus,
                     GameSettings settings) {
    this.players = Lists.newArrayList(players);
    this.status = status;
    this.eventBus = eventBus;
    this.settings = settings;
  }

  public boolean kickPlayer(GamePlayer player) {
    if (PlayerUtils.contains(players, player)) {
      eventBus.postGameExit(this, player);
      return true;
    }
    return false;
  }

  public boolean joinPlayer(GamePlayer player) {
    if (!PlayerUtils.contains(players, player)) {
      eventBus.postGameJoin(this, player);
      return true;
    }

    return false;
  }

  public GamePlayer getPlayer(UUID playerId) {
    return players.stream().filter(p -> p.getPlayer().getUniqueId().equals(playerId)).findAny()
        .orElse(null);
  }
}
