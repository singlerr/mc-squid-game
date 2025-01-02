package io.github.singlerr.admin;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.utils.Transform;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

@UtilityClass
public class ModelUtils {

  public void setModel(ArmorStand armorStand, String modelLocation, Transform transform) {
    armorStand.customName(Component.text(armorStand.getUniqueId().toString()));
    armorStand.setCustomNameVisible(true);
    PacketInitModel pkt =
        new PacketInitModel(armorStand.getUniqueId(), armorStand.getEntityId(),
            transform,
            modelLocation);
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendToAll(pkt);
  }
}
