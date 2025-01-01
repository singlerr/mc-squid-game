package io.github.singlerr.dalgona;

import io.github.singlerr.dalgona.game.DalgonaGame;
import io.github.singlerr.dalgona.network.PacketDalgonaRequest;
import io.github.singlerr.dalgona.network.PacketDalgonaResult;
import io.github.singlerr.dalgona.network.handler.PacketDalgonaResultHandler;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketRegistry;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Dalgona extends JavaPlugin {

  private DalgonaGame game;

  @Override
  public void onEnable() {
    RegisteredServiceProvider<GameRegistry> registry =
        getServer().getServicesManager().getRegistration(GameRegistry.class);
    RegisteredServiceProvider<NetworkRegistry> networkRegistry =
        getServer().getServicesManager().getRegistration(NetworkRegistry.class);
    if (registry == null || networkRegistry == null) {
      throw new IllegalStateException("Game core does not exist!");
    }
    game = new DalgonaGame();
    registry.getProvider().register("mgr", game);
    registerPackets(networkRegistry.getProvider());
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
  }

  private void registerPackets(NetworkRegistry registry) {
    registry.register(PacketDalgonaResult.ID,
        PacketRegistry.createRegistry(PacketDalgonaResult.class, PacketDalgonaResult::new,
            new PacketDalgonaResultHandler(game)));
    registry.register(PacketDalgonaRequest.ID,
        PacketRegistry.createRegistry(PacketDalgonaRequest.class, PacketDalgonaRequest::new,
            (a, b) -> {
            }));
  }
}
