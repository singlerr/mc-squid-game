package io.github.singlerr.trolley.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.trolley.game.Ticker;
import io.github.singlerr.trolley.game.TrolleyGame;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@RequiredArgsConstructor
public final class TrolleyEventListener extends InteractableListener {

  private final TrolleyGame game;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (game == null || game.getContext() == null) {
      return;
    }
    GameContext context = game.getContext();
    GamePlayer player = context.getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(GameCore.getInstance(), () -> {

      if (!player.available()) {
        return;
      }
      player.getPlayer().setCustomNameVisible(true);
      context.syncName(player, context.getPlayers());
      context.syncName(context.getPlayers(), player);
    }, 20L);
  }

  @EventHandler
  public void banPlayer(PlayerRespawnEvent event) {
    if (!game.getContext().shouldBanOnRespawn(event.getPlayer())) {
      game.getContext().respawnTroy(event.getPlayer());
      Location spawnLoc = GameCore.getInstance().getSpawnLocation();
      if (spawnLoc != null) {
        event.setRespawnLocation(spawnLoc);
      }
      return;
    }
    if (Bukkit.getServer().getBanList(BanListType.PROFILE)
        .isBanned(event.getPlayer().getPlayerProfile())) {
      event.getPlayer().kick(Component.text("오징어 게임에서 탈락했습니다!"));
    }
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    GamePlayer p = game.getContext().getPlayer(event.getPlayer().getUniqueId());
    if (p == null) {
      return;
    }

    if (p.getRole().getLevel() < GameRole.ADMIN.getLevel()) {
      game.getContext().kickPlayer(p);
    }
  }

  @EventHandler
  public void onJump(PlayerJumpEvent event) {
    GamePlayer p = game.getContext().getPlayer(event.getPlayer().getUniqueId());
    if (p == null) {
      return;
    }

    Ticker t = game.getContext().getPlayerStatus(event.getPlayer().getUniqueId());
    if (t == null) {
      return;
    }

    if (!t.shouldTick()) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    GameContext context = game.getContext();
    GamePlayer player = context.getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }

    for (GamePlayer p : context.getPlayers()) {
      if (!p.available()) {
        continue;
      }
      Component prefix =
          p.getRole().getLevel() >= GameRole.ADMIN.getLevel() ? player.getAdminDisplayName() :
              player.getUserDisplayName();
      p.sendMessage(prefix.append(Component.text(" : ")).append(event.message()));
    }

    event.setCancelled(true);
  }


  @EventHandler
  public void removeDeathMessage(PlayerDeathEvent event) {
    event.deathMessage(null);
  }

}
