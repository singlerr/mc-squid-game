package io.github.singlerr.sg.rlgl;

import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.rlgl.game.RLGLGame;
import io.github.singlerr.sg.rlgl.network.PacketAnimateTransformation;
import io.github.singlerr.sg.rlgl.network.PacketTransformModel;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class RedLightGreenLight extends JavaPlugin {

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }

    registry.getProvider().register("rlgl", new RLGLGame());
    registerPackets(networkRegistry.getProvider());
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  private void registerPackets(NetworkRegistry registry) {
    registry.register(PacketAnimateTransformation.ID, PacketAnimateTransformation.class);
    registry.register(PacketTransformModel.ID, PacketTransformModel.class);
  }
}
