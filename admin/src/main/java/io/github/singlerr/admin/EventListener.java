package io.github.singlerr.admin;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.core.utils.Transform;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class EventListener extends InteractableListener {

  private static final int MP5_MODEL_DATA = 483002;
  private static final String SOUND_MP5 = "mp5.shot";
  private final Admin plugin;

  @EventHandler
  public void killPlayer(PlayerInteractAtEntityEvent event) {
    if (!(event.getRightClicked() instanceof Player player)) {
      return;
    }

    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    ItemStack item =
        event.getPlayer().getInventory().getItem(event.getPlayer().getActiveItemHand());

    if (item.getItemMeta() == null) {
      return;
    }

    if (!item.getItemMeta().hasCustomModelData()) {
      return;
    }

    if (item.getType() != Material.GOLDEN_AXE &&
        item.getItemMeta().getCustomModelData() != MP5_MODEL_DATA) {
      return;
    }

    GameLifecycle lifecycle = GameCore.getInstance().getCoreLifecycle();
    if (lifecycle == null) {
      return;
    }
    GameLifecycle.GameInfo info = lifecycle.getCurrentGame();
    if (info == null) {
      return;
    }
    if (info.context() == null) {
      return;
    }
    GamePlayer p = info.context().getPlayer(player.getUniqueId());
    if (p == null) {
      return;
    }
    if (!p.available()) {
      return;
    }
    if (p.getRole().getLevel() > GameRole.TROY.getLevel()) {
      return;
    }

    GamePlayer killer = info.context().getPlayer(event.getPlayer().getUniqueId());
    if (killer == null) {
      return;
    }

    if (killer.getRole().getLevel() < GameRole.ADMIN.getLevel()) {
      return;
    }

    event.getPlayer().getWorld().playSound(event.getPlayer().getLocation(), SOUND_MP5, 1f, 1f);
    player.setVelocity(event.getPlayer().getLocation().getDirection().multiply(0.3));

    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      player.setHealth(0);
    }, 5L);

  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onJoin(PlayerJoinEvent event) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
      NetworkRegistry network =
          Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
      for (EntityReference e : ModelTrackers.entitiesNotNull()) {
        Display display =
            e.getEntity().getPassengers().stream().filter(entity -> entity instanceof Display)
                .map(entity -> (Display) entity).findAny().orElse(null);
        if (display == null) {
          continue;
        }
        PacketInitModel pkt = new PacketInitModel(display.getUniqueId(), display.getEntityId(),
            new Transform(null, null, null),
            e.getModelLocation());

        network.getChannel().sendTo(event.getPlayer(), pkt);
      }
    }, 50L);
  }
}
