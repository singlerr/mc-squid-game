package io.github.singlerr.sg.core.context;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class GamePlayer implements Comparable<GamePlayer> {


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

  @Override
  public int compareTo(@NotNull GamePlayer o) {
    return equals(o) ? 0 : 1;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GamePlayer player1 = (GamePlayer) o;
    if (player.getPlayer() == null || player1.getPlayer() == null) {
      return false;
    }

    return Objects.equals(player.getPlayer().getUniqueId(), player1.getPlayer().getUniqueId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(player.getUniqueId());
  }
}
