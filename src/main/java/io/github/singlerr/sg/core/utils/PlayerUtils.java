package io.github.singlerr.sg.core.utils;

import com.mojang.authlib.GameProfile;
import io.github.singlerr.sg.core.context.GamePlayer;
import java.util.Collection;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Player;

@UtilityClass
public class PlayerUtils {

  public boolean contains(Collection<Player> players, Player player) {
    return players.stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
  }

  public boolean contains(Collection<GamePlayer> players, GamePlayer player) {
    return players.stream()
        .anyMatch(p -> p.getPlayer().getUniqueId().equals(player.getPlayer().getUniqueId()));
  }

  public void changeSkin(Player player, String skinUrl) {
    GameProfile profile = new GameProfile(player.getUniqueId(), player.getName());
    //TODO
  }
}
