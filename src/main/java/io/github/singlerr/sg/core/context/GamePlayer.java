package io.github.singlerr.sg.core.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;

@Data
@AllArgsConstructor
public class GamePlayer {


  private final Player player;
  private GameRole role;

  public static GamePlayer ofUser(Player player) {
    return new GamePlayer(player, GameRole.USER);
  }

  public static GamePlayer ofAdmin(Player player) {
    return new GamePlayer(player, GameRole.ADMIN);
  }

  public static GamePlayer ofSpectator(Player player) {
    return new GamePlayer(player, GameRole.SPECTATOR);
  }

  public boolean shouldInteract() {
    return role == GameRole.USER;
  }
}
