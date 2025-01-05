package io.github.singlerr.sg.core.utils;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

@UtilityClass
public class ModelUtils {

  public void setModel(Entity display, String modelLocation, Transform transform) {
    display.customName(Component.text(display.getUniqueId().toString()));
    display.setCustomNameVisible(true);
    PacketInitModel pkt =
        new PacketInitModel(display.getUniqueId(), display.getEntityId(),
            transform,
            modelLocation);
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendToAll(pkt);
  }

  public void setModel(Entity display, String modelLocation) {
    setModel(display, modelLocation, new Transform(null, null, null));
  }
}
