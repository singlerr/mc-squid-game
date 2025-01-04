package io.github.singlerr.sg.rlgl.listener;

import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import io.github.singlerr.sg.rlgl.game.RLGLGame;
import io.github.singlerr.sg.rlgl.game.RLGLGameContext;
import io.github.singlerr.sg.rlgl.game.RLGLGameSettings;
import io.github.singlerr.sg.rlgl.game.RLGLItemRole;
import io.github.singlerr.sg.rlgl.game.RLGLStatus;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

@Slf4j
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
    if (!player.available()) {
      return;
    }
    if (player.getRole().getLevel() >= GameRole.ADMIN.getLevel()) {
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
      Component msg =
          Component.text("플레이어 움직임 감지: [").append(game.getGameContext().getKillTargets().stream()
                  .map(i -> game.getGameContext().getPlayer(i).getAdminDisplayName())
                  .reduce((a, b) -> a.append(Component.text(",")).append(b)).get())
              .append(Component.text("]"));
      Collection<GamePlayer> players = game.getGameContext().getPlayers(GameRole.ADMIN);
      PlayerUtils.setGlowing(player.getPlayer(), players.stream().filter(GamePlayer::available).map(
          GamePlayer::getPlayer).toList(), true);
      for (GamePlayer gamePlayer : players) {
        gamePlayer.getPlayer()
            .sendMessage(msg.style(Style.style(NamedTextColor.RED)));
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
  public void onJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = game.getGameContext().getPlayer(player.getUniqueId())) != null) {
//      game.getGameContext().
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

    if (!player.available()) {
      return;
    }

    itemRole.execute(game.getGameContext(), player);
    infoCallback(player.getPlayer(), "실행: {}", itemRole.getDisplayName().getString());
    event.setCancelled(true);
  }

}
