package io.github.singlerr.intermediary.listener;

import io.github.singlerr.intermediary.game.IntermediaryGame;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public final class IntermediaryEventListener extends InteractableListener {

  private final IntermediaryGame context;

  public IntermediaryEventListener(IntermediaryGame context) {
    this.context = context;
  }

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (context == null || context.getContext() == null) {
      return;
    }
    GameContext context = this.context.getContext();
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
  public void onDeath(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getContext().getPlayer(player.getUniqueId())) != null) {
      context.getContext().kickPlayer(gamePlayer);
    }

  }

  @EventHandler
  public void banPlayer(PlayerRespawnEvent event) {
    if (Bukkit.getServer().getBanList(BanListType.PROFILE)
        .isBanned(event.getPlayer().getPlayerProfile())) {
      event.getPlayer().kick(Component.text("오징어 게임에서 탈락했습니다!"));
    }
  }


  @EventHandler
  public void removeDeathMessage(PlayerDeathEvent event) {
    event.deathMessage(null);
  }

  @EventHandler
  public void onChat(AsyncChatEvent event) {
    GameContext context = this.context.getContext();
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

}
