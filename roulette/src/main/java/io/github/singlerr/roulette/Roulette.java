package io.github.singlerr.roulette;

import io.github.singlerr.roulette.game.RouletteGame;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Roulette extends JavaPlugin {

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }
    registry.getProvider().register("roulette", new RouletteGame());
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
