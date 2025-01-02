package io.github.singlerr.dalgona.listener;

import io.github.singlerr.dalgona.game.DalgonaGame;
import io.github.singlerr.dalgona.game.DalgonaGameContext;
import io.github.singlerr.dalgona.game.DalgonaGameSettings;
import io.github.singlerr.dalgona.game.DalgonaGameStatus;
import io.github.singlerr.dalgona.game.PlayerDalgonaStatus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.utils.InteractableListener;
import lombok.AllArgsConstructor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

@AllArgsConstructor
public final class DalgonaEventListener extends InteractableListener {

  private final DalgonaGame game;

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
    if ((status = context.getStatus(gamePlayer.getId())) == null) {
      return;
    }

    if (status != PlayerDalgonaStatus.IDLE) {
      return;
    }

    context.beginDalgona(gamePlayer.getPlayer());

    event.setCancelled(true);
  }
}
