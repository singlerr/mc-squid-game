package io.github.singlerr.sg.rlgl;

import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.rlgl.game.RLGLGame;
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
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  private void registerPackets(NetworkRegistry registry) {
  }
}
