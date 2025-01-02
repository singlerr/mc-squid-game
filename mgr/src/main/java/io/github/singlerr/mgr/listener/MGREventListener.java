package io.github.singlerr.mgr.listener;

import io.github.singlerr.mgr.game.MGRGame;
import io.github.singlerr.mgr.game.MGRGameContext;
import io.github.singlerr.mgr.game.MGRGameStatus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@RequiredArgsConstructor
public final class MGREventListener extends InteractableListener {

  private final MGRGame game;

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    MGRGameContext context = game.getContext();
    GamePlayer gamePlayer = context.getPlayer(event.getPlayer().getUniqueId());
    if (gamePlayer == null) {
      return;
    }
    if (block.getState() instanceof Powerable) {
      if (gamePlayer.getRole().getLevel() <= GameRole.TROY.getLevel() &&
          context.getGameStatus() == MGRGameStatus.CLOSING_ROOM) {
        event.setCancelled(true);
      }
    } else if (block.getState() instanceof Door door) {
      if (gamePlayer.getRole() == GameRole.ADMIN) {
        door.setOpen(!door.isOpen());
      }
    }
  }

  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    MGRGameContext context = game.getContext();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getPlayer(player.getUniqueId())) != null) {
      context.kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = game.getContext().getPlayer(player.getUniqueId())) != null) {
      game.getContext().kickPlayer(gamePlayer);
    }
  }

}
