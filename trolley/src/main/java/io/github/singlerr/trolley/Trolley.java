package io.github.singlerr.trolley;

import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketRegistry;
import io.github.singlerr.trolley.game.TrolleyGame;
import io.github.singlerr.trolley.network.PacketIntermissionRequest;
import io.github.singlerr.trolley.network.PacketIntermissionResult;
import io.github.singlerr.trolley.network.handler.PacketIntermissionResultHandler;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Trolley extends JavaPlugin {

  private TrolleyGame game;

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }
    game = new TrolleyGame();
    networkRegistry.getProvider().register(PacketIntermissionRequest.ID,
        PacketRegistry.createRegistry(PacketIntermissionRequest.class,
            PacketIntermissionRequest::new, (pkt, p) -> {
            }));
    networkRegistry.getProvider().register(PacketIntermissionResult.ID,
        PacketRegistry.createRegistry(PacketIntermissionResult.class, PacketIntermissionResult::new,
            new PacketIntermissionResultHandler(game)));
    registry.getProvider().register("trolley", game);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }
}
