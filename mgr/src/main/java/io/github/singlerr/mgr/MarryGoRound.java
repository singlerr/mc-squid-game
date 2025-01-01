package io.github.singlerr.mgr;

import io.github.singlerr.mgr.game.MGRGame;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class MarryGoRound extends JavaPlugin {

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }

    registry.getProvider().register("mgr", new MGRGame());
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
