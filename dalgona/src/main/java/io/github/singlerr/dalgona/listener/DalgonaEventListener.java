package io.github.singlerr.dalgona.listener;

import io.github.singlerr.dalgona.game.DalgonaGame;
import io.github.singlerr.dalgona.game.DalgonaGameContext;
import io.github.singlerr.dalgona.game.DalgonaGameSettings;
import io.github.singlerr.dalgona.game.DalgonaGameStatus;
import io.github.singlerr.dalgona.game.PlayerDalgonaStatus;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.event.player.AsyncChatEvent;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public final class DalgonaEventListener extends InteractableListener {

  private final DalgonaGame game;

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
    if (Bukkit.getServer().getBanList(BanListType.PROFILE)
        .isBanned(event.getPlayer().getPlayerProfile())) {
      event.getPlayer().kick(Component.text("오징어 게임에서 탈락했습니다!"));
    }
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
  public void onInteract(PlayerInteractEvent event) {
    DalgonaGameContext context = game.getContext();
    if (context.getGameStatus() != DalgonaGameStatus.PROGRESS) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    if (event.getClickedBlock() == null) {
      return;
    }

    Material desiredType = ((DalgonaGameSettings) context.getSettings()).getDalgonaType();
    if (event.getClickedBlock().getType() != desiredType) {
      return;
    }

    GamePlayer gamePlayer = context.getPlayer(event.getPlayer().getUniqueId());
    if (gamePlayer == null) {
      return;
    }

    if (!gamePlayer.available()) {
      return;
    }

    PlayerDalgonaStatus status;
    if ((status = context.getPlayerStatus(gamePlayer.getId())) == null) {
      return;
    }

    if (status == PlayerDalgonaStatus.SUCCESS) {
      return;
    }

    context.beginDalgona(gamePlayer.getPlayer());

    event.setCancelled(true);
  }

  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = game.getContext().getPlayer(player.getUniqueId())) != null) {
      game.getContext().kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void removeDeathMessage(PlayerDeathEvent event) {
    event.deathMessage(null);
  }

}
