package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

@Slf4j
public class RLGLGameSetupContext extends GameSetupContext<RLGLGameSettings> {

  public RLGLGameSetupContext(RLGLGameSettings settings) {
    super(settings);
  }

  public void setYoungHee(ArmorStand armorStand) {
    armorStand.customName(Component.text(armorStand.getUniqueId().toString()));
    armorStand.setCustomNameVisible(true);
    PacketInitModel pkt =
        new PacketInitModel(armorStand.getUniqueId(), armorStand.getEntityId(),
            getSettings().getFrontState(),
            getSettings().getModelLocation());
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendToAll(pkt);
    getSettings().setYoungHee(EntitySerializable.of(armorStand));
  }
}
