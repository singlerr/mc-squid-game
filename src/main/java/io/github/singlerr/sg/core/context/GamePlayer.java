package io.github.singlerr.sg.core.context;

import java.util.Objects;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Data
@AllArgsConstructor
public class GamePlayer implements Comparable<GamePlayer> {

  private UUID id;
  private Player player;
  private GameRole role;
  private Component userDisplayName;
  private Component adminDisplayName;

  private Gender gender;
  private int userNumber;

  public GamePlayer(Player player, GameRole role, Gender gender) {
    this(player.getUniqueId(), player, role, Component.text(player.getName()),
        Component.text(player.getName()), gender, 0);

    if (role.getLevel() >= GameRole.ADMIN.getLevel()) {
      setUserDisplayName(Component.text("?"));
    }
  }

  public static GamePlayer ofUser(Player player, Gender gender) {
    return new GamePlayer(player, GameRole.USER, gender);
  }

  public static GamePlayer ofAdmin(Player player, Gender gender) {
    return new GamePlayer(player, GameRole.ADMIN, gender);
  }

  public static GamePlayer ofTroy(Player player, Gender gender) {
    return new GamePlayer(player, GameRole.TROY, gender);
  }

  public boolean available() {
    player = Bukkit.getPlayer(id);
    return player != null;
  }

  public void sendMessage(Component component) {
    if (player != null) {
      player.sendMessage(component);
    }
  }

  public boolean shouldInteract() {
    return role.getLevel() <= GameRole.USER.getLevel();
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
    return Objects.equals(id, player1.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
