package io.github.singlerr.trolley.listener;

import io.github.singlerr.sg.core.setup.helpers.RegionBuilder;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.trolley.game.TrolleyGameSetup;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class TrolleyGameSetupListener extends InteractableListener {

  private final TrolleyGameSetup setup;

  @EventHandler
  public void buildGameRegion(PlayerInteractEvent event) {
    if (event.getItem() == null) {
      return;
    }
    if (event.getClickedBlock() == null) {
      return;
    }
    if (event.getItem().getType() != Material.WOODEN_AXE) {
      return;
    }

    if (setup.getContext().getGameRegionBuilder(event.getPlayer().getUniqueId()) == null) {
      return;
    }

    RegionBuilder regionBuilder =
        setup.getContext().getGameRegionBuilder(event.getPlayer().getUniqueId());
    Player player = event.getPlayer();

    switch (event.getAction()) {
      case LEFT_CLICK_BLOCK -> {
        if (regionBuilder
            .setStart(event.getClickedBlock().getLocation())) {
          successCallback(player, "Start point set: {}", event.getClickedBlock().getLocation());
        }
        event.setCancelled(true);
      }
      case RIGHT_CLICK_BLOCK -> {
        if (regionBuilder
            .setEnd(event.getClickedBlock().getLocation())) {
          setup.getContext().setGameRegion(regionBuilder.build());
          successCallback(player, "End point set: {}", event.getClickedBlock().getLocation());
          successCallback(player, "Successfully created game region");
        } else {
          errorCallback(player, "Set the first point first");
        }
        event.setCancelled(true);
      }
      default -> {

      }
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (event.getItem() == null) {
      return;
    }
    if (event.getClickedBlock() == null) {
      return;
    }
    if (event.getItem().getType() != Material.WOODEN_AXE) {
      return;
    }

    if (setup.getContext().getTrackNumber(event.getPlayer().getUniqueId()) == null ||
        setup.getContext().getRegionBuilder(event.getPlayer().getUniqueId()) == null) {
      return;
    }

    RegionBuilder regionBuilder =
        setup.getContext().getRegionBuilder(event.getPlayer().getUniqueId());
    int trackNum = setup.getContext().getTrackNumber(event.getPlayer().getUniqueId());
    Player player = event.getPlayer();

    switch (event.getAction()) {
      case LEFT_CLICK_BLOCK -> {
        if (regionBuilder
            .setStart(event.getClickedBlock().getLocation())) {
          successCallback(player, "Start point set: {}", event.getClickedBlock().getLocation());
        }
        event.setCancelled(true);
      }
      case RIGHT_CLICK_BLOCK -> {
        if (regionBuilder
            .setEnd(event.getClickedBlock().getLocation())) {
          setup.getContext().setTrainTrack(trackNum, regionBuilder.build());
          successCallback(player, "End point set: {}", event.getClickedBlock().getLocation());
          successCallback(player, "Successfully created track");
        } else {
          errorCallback(player, "Set the first point first");
        }
        event.setCancelled(true);
      }
      default -> {

      }
    }
  }

  @EventHandler
  public void onInteractEntity(PlayerInteractAtEntityEvent event) {
    ItemStack item = event.getPlayer().getActiveItem();
    if (item == null) {
      return;
    }
    if (item.getType() != Material.WOODEN_AXE) {
      return;
    }

    if (setup.getContext().getTrackNumber(event.getPlayer().getUniqueId()) == null) {
      return;
    }

    int trackNum = setup.getContext().getTrackNumber(event.getPlayer().getUniqueId());
    setup.getContext().setTrainEntity(trackNum, event.getRightClicked());

    successCallback(event.getPlayer(), "Train entity set : {}", trackNum);
    event.setCancelled(true);
  }
}
