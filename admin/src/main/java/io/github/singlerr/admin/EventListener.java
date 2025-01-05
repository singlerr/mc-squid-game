package io.github.singlerr.admin;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.core.utils.ModelUtils;
import io.github.singlerr.sg.core.utils.Transform;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class EventListener extends InteractableListener {

  @EventHandler
  public void onInteract(PlayerInteractAtEntityEvent event) {
    Entity interaction = event.getRightClicked();
    if (!(interaction instanceof Interaction e)) {
      return;
    }

    Display display = e.getPassengers().stream().filter(entity -> entity instanceof Display)
        .map(entity -> (Display) entity).findAny().orElse(null);
    if (display == null) {
      return;
    }

    CommandContexts.Context context = CommandContexts.getContext(event.getPlayer().getUniqueId());
    if (context == null) {
      return;
    }

    ModelUtils.setModel(display, context.modelLocation(), context.transform());
    ModelTrackers.addEntity(context.modelLocation(), interaction);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onJoin(PlayerJoinEvent event) {
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    for (EntityReference e : ModelTrackers.entitiesNotNull()) {
      Display display =
          e.getEntity().getPassengers().stream().filter(entity -> entity instanceof Display)
              .map(entity -> (Display) entity).findAny().orElse(null);
      if (display == null) {
        continue;
      }
      network.getChannel().sendTo(event.getPlayer(),
          new PacketInitModel(display.getUniqueId(), display.getEntityId(),
              new Transform(null, null, null),
              e.getModelLocation()));
    }
  }
}
