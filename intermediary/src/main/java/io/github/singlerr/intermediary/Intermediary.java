package io.github.singlerr.intermediary;

import io.github.singlerr.intermediary.game.IntermediaryGame;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
public final class Intermediary extends JavaPlugin {

  @Getter
  private static Intermediary instance;


  private IntermediaryGame game;

  public Intermediary() {
    instance = this;
  }

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }

    game = new IntermediaryGame();
    registry.getProvider().register("intermediary", game);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
