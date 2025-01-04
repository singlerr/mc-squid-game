package io.github.singlerr.sg.core.utils;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
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
    if (role == GameRole.ADMIN) {
      changeSkin(player, GameCore.getInstance().getAdminSkinUrl());
    } else {
      List<String> urls = GameCore.getInstance().getPlayerSkinUrl();
      int idx = random.nextInt(urls.size());
      changeSkin(player, urls.get(idx));
    }
  }

  public void changeSkin(Player player, String skinUrl) {
    SkinsRestorer api = SkinsRestorerProvider.get();
    SkinStorage storage = api.getSkinStorage();
    try {
      Optional<InputDataResult> result = storage.findOrCreateSkinData(skinUrl);
      if (result.isEmpty()) {
        log.error("Failed to apply skin {}", skinUrl);
        return;
      }

      PlayerStorage playerStorage = api.getPlayerStorage();
      playerStorage.setSkinIdOfPlayer(player.getUniqueId(), result.get().getIdentifier());
      api.getSkinApplier(Player.class).applySkin(player);
      log.info("Changed {} skin to {}", player.getName(), skinUrl);
    } catch (DataRequestException | MineSkinException e) {
      log.error("Failed to apply skin", e);
    }
  }
}
