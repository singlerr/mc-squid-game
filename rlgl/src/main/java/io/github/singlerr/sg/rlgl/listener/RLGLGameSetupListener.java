package io.github.singlerr.sg.rlgl.listener;

import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.rlgl.game.RLGLGameSetupContext;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;

public final class RLGLGameSetupListener extends InteractableListener {

  private final RLGLGameSetupContext context;
  private final GameSetupManager setupManager;

  public RLGLGameSetupListener(GameSetupManager setupManager, RLGLGameSetupContext context) {
    this.setupManager = setupManager;
    this.context = context;
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    if (!setupManager.isOnContext(player.getUniqueId())) {
      return;
    }

    if (event.getClickedBlock() == null) {
      return;
    }

    switch (event.getAction()) {
      case LEFT_CLICK_BLOCK -> {
        if (context.getSetupHelper().getRegionBuilder()
            .setStart(event.getClickedBlock().getLocation())) {
          successCallback(player, "Start point set: {}", event.getClickedBlock().getLocation());
        }
        event.setCancelled(true);
        return;
      }
      case RIGHT_CLICK_BLOCK -> {
        if (context.getSetupHelper().getRegionBuilder()
            .setEnd(event.getClickedBlock().getLocation())) {
          successCallback(player, "End point set: {}", event.getClickedBlock().getLocation());
        } else {
          errorCallback(player, "Set the first point first");
        }
        event.setCancelled(true);
      }
      default -> {

      }
    }
  }
}
