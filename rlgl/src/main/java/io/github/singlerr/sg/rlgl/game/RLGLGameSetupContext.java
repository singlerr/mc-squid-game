package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;

@Slf4j
public class RLGLGameSetupContext extends GameSetupContext<RLGLGameSettings> {

  public RLGLGameSetupContext(RLGLGameSettings settings) {
    super(settings);
  }

  public void setYoungHee(Interaction armorStand) {
    Display display =
        armorStand.getPassengers().stream().filter(e -> e instanceof Display).map(e -> (Display) e)
            .findAny().orElse(null);
    if (display == null) {
      log.info("Display {} is null", armorStand);
      return;
    }
    display.customName(Component.text(display.getUniqueId().toString()));
    display.setCustomNameVisible(true);
    PacketInitModel pkt =
        new PacketInitModel(display.getUniqueId(), display.getEntityId(),
            getSettings().getFrontState(),
            getSettings().getModelLocation());
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendToAll(pkt);
    getSettings().setYoungHee(EntitySerializable.of(armorStand));
  }
}
