package io.github.singlerr.sg.core.utils;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.skinsrestorer.api.SkinsRestorerAPI;
import net.skinsrestorer.api.exception.SkinRequestException;
import org.bukkit.entity.Player;

@Slf4j
@UtilityClass
public class PlayerUtils {

  private SecureRandom random = new SecureRandom();

  public boolean contains(Collection<Player> players, Player player) {
    return players.stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
  }

  public boolean contains(Collection<GamePlayer> players, GamePlayer player) {
    return players.stream()
        .anyMatch(p -> p.getId().equals(player.getId()));
  }

  public void changeSkin(Player player, GameRole role) {
    if (Objects.requireNonNull(role) == GameRole.ADMIN) {
      changeSkin(player, GameCore.getInstance().getAdminSkinUrl());
    } else {
      List<String> urls = GameCore.getInstance().getPlayerSkinUrl();
      int idx = random.nextInt(urls.size());
      changeSkin(player, urls.get(idx));
    }
  }

  public void changeSkin(Player player, String skinUrl) {
    try {
      SkinsRestorerAPI.getApi().setSkin(player.getName(), skinUrl);
    } catch (SkinRequestException e) {
      log.error("Failed to change skin", e);
    }
  }
}
