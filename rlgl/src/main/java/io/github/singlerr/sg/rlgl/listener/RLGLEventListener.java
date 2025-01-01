package io.github.singlerr.sg.rlgl.listener;

import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.rlgl.game.RLGLGame;
import io.github.singlerr.sg.rlgl.game.RLGLGameContext;
import io.github.singlerr.sg.rlgl.game.RLGLGameSettings;
import io.github.singlerr.sg.rlgl.game.RLGLItemRole;
import io.github.singlerr.sg.rlgl.game.RLGLStatus;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.slf4j.helpers.MessageFormatter;

public final class RLGLEventListener extends InteractableListener {

  private final RLGLGame game;

  public RLGLEventListener(RLGLGame game) {
    this.game = game;
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    RLGLGameContext context = game.getGameContext();
    GamePlayer player = context.getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }
    if (!player.shouldInteract()) {
      return;
    }
    if (game.getGameContext().getRlglStatus() != RLGLStatus.RED_LIGHT) {
      return;
    }

    RLGLGameSettings settings = (RLGLGameSettings) context.getSettings();
    if (!settings.getDeadRegion().isIn(player.getPlayer().getLocation())) {
      return;
    }

    float moveDist = (float) event.getFrom().distance(event.getTo());
    float rotDist = (float) event.getFrom().getDirection().distance(event.getTo().getDirection());
    if (moveDist >= settings.getKillSwitch() || rotDist >= settings.getKillSwitch()) {
      game.getGameContext().getKillTargets().add(player.getPlayer().getUniqueId());
      String msg = MessageFormatter.basicArrayFormat("플레이어 움직임 감지: {}",
          game.getGameContext().getKillTargets().stream()
              .map(i -> context.getPlayer(i).getAdminDisplayName().append(Component.text("-"))
                  .append(Component.text(context.getPlayer(i).getPlayer().getName()))).toArray());
      player.getPlayer().setGlowing(true);
      for (GamePlayer gamePlayer : game.getGameContext().getPlayers()) {
        if (gamePlayer.getRole() == GameRole.ADMIN) {
          gamePlayer.getPlayer()
              .sendMessage(Component.text(msg).style(Style.style(NamedTextColor.RED)));
        }
      }
    }
  }

  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = game.getGameContext().getPlayer(player.getUniqueId())) != null) {
      game.getGameContext().kickPlayer(gamePlayer);
    }
  }


  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = game.getGameContext().getPlayer(player.getUniqueId())) != null) {
      game.getGameContext().kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!(event.getAction() == Action.RIGHT_CLICK_AIR ||
        event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
      return;
    }

    GamePlayer player = game.getGameContext().getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }

    if (player.getRole() != GameRole.ADMIN) {
      return;
    }

    if (event.getItem() == null) {
      return;
    }

    RLGLItemRole itemRole = RLGLItemRole.getRole(event.getItem());
    if (itemRole == null) {
      return;
    }

    itemRole.execute(game.getGameContext(), player);
    infoCallback(player.getPlayer(), "실행: {}", itemRole.getDisplayName().getString());
    event.setCancelled(true);
  }

}
