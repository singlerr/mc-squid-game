package io.github.singlerr.admin;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.utils.InteractableListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class EventListener extends InteractableListener {

  @EventHandler
  public void onInteract(PlayerInteractAtEntityEvent event) {
    if (!(event.getRightClicked() instanceof ArmorStand display)) {
      return;
    }

    CommandContexts.Context context = CommandContexts.getContext(event.getPlayer().getUniqueId());
    if (context == null) {
      return;
    }

    ModelUtils.setModel(display, context.modelLocation(), context.transform());
    ModelTrackers.addEntity(display.getUniqueId(), context.modelLocation(), display,
        context.transform());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    for (EntityReference e : ModelTrackers.entitiesNotNull()) {
      network.getChannel().sendTo(event.getPlayer(),
          new PacketInitModel(e.getId(), e.getEntity().getEntityId(), e.getTransform(),
              e.getModelLocation()));
    }
  }
}
