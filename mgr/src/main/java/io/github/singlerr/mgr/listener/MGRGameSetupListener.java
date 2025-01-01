package io.github.singlerr.mgr.listener;

import io.github.singlerr.mgr.game.MGRGameSetupContext;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.utils.InteractableListener;
import java.util.function.Consumer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public final class MGRGameSetupListener extends InteractableListener {

  private final GameSetupManager setupManager;
  private final MGRGameSetupContext context;

  public MGRGameSetupListener(GameSetupManager setupManager, MGRGameSetupContext context) {
    this.setupManager = setupManager;
    this.context = context;
  }

  @EventHandler
  public void selectDoor(PlayerInteractEvent event) {
    ItemStack item = event.getItem();
    if (item == null) {
      return;
    }
    if (event.getInteractionPoint() == null) {
      return;
    }

    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    Player p = event.getPlayer();
    Consumer<Location> executor = context.getDoorSetupContext(p.getUniqueId());
    if (executor == null) {
      return;
    }
    executor.accept(event.getInteractionPoint());
    successCallback(p, "설정 완료");
  }

  @EventHandler
  public void selectPillar(PlayerInteractAtEntityEvent event) {
    ItemStack item = event.getPlayer().getActiveItem();
    if (item == null) {
      return;
    }

    if (item.getType() != Material.BLAZE_ROD) {
      return;
    }

    Player p = event.getPlayer();
    Consumer<Entity> executor = context.getPillarSetupContext(p.getUniqueId());
    if (executor == null) {
      return;
    }

    executor.accept(event.getRightClicked());
    successCallback(p, "설정 완료");
    event.setCancelled(true);
  }

  @EventHandler
  public void selectRegion(PlayerInteractEvent event) {
    MGRGameSetupContext.RegionSelector regionSelector =
        context.getSelector(event.getPlayer().getUniqueId());
    if (regionSelector == null) {
      return;
    }

    ItemStack item = event.getItem();
    if (item == null) {
      return;
    }

    if (item.getType() != Material.WOODEN_AXE) {
      return;
    }

    if (event.getInteractionPoint() == null) {
      return;
    }

    Location loc = event.getInteractionPoint();
    Player player = event.getPlayer();
    switch (event.getAction()) {
      case LEFT_CLICK_BLOCK -> {
        if (regionSelector.getBuilder()
            .setStart(loc)) {
          successCallback(player, "Start point set: {}", loc);
        }
        event.setCancelled(true);
      }
      case RIGHT_CLICK_BLOCK -> {
        if (event.getItem() == null || event.getItem().getType() != Material.STICK) {
          return;
        }
        if (regionSelector.getBuilder()
            .setEnd(loc)) {
          context.getSettings().getRooms()
              .put(regionSelector.getRoomNum(), regionSelector.getBuilder().build());
          successCallback(player, "End point set: {}", loc);
          successCallback(player, "Successfully created room {}", regionSelector.getRoomNum());
        } else {
          errorCallback(player, "Set the first point first");
        }
        event.setCancelled(true);
      }
      default -> {
        return;
      }
    }

  }

}
