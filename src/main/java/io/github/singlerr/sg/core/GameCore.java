package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.commands.GameCommands;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.impl.PluginAwareNetworkRegistry;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.registry.impl.DefaultGameRegistry;
import io.github.singlerr.sg.core.registry.impl.RegistryFactory;
import java.io.File;
import java.io.IOException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

@Slf4j
public final class GameCore extends JavaPlugin {

  @Getter
  private static GameCore instance;
  private GameStorage settingsStorage;

  private NetworkRegistry networkRegistry;
  private GameRegistry gameRegistry;
  private GameLifecycle coreLifecycle;
  private GameSetupManager setupManager;

  public GameCore() {
    GameCore.instance = this;
  }

  @Override
  public void onEnable() {
    loadSettings();
    this.setupManager = new GameSetupManager(gameRegistry);
    setup(gameRegistry);
    this.coreLifecycle =
        new GameLifecycle(gameRegistry, settingsStorage.getLoadedSettings(), instance);
    this.coreLifecycle.runTaskTimerAsynchronously(instance, 0L, 1L);
    ((PluginAwareNetworkRegistry) networkRegistry).registerToMessengers();
    registerCommands();
  }

  @Override
  public void onLoad() {
    this.networkRegistry = new PluginAwareNetworkRegistry(instance);
    this.gameRegistry = DefaultGameRegistry.create();
    Bukkit.getServicesManager().register(GameRegistry.class, gameRegistry, instance,
        ServicePriority.Highest);
    Bukkit.getServicesManager()
        .register(NetworkRegistry.class, networkRegistry, instance, ServicePriority.Highest);
  }

  private void registerCommands() {
    GameCommands gameCmd = new GameCommands(coreLifecycle, gameRegistry, setupManager);
    getCommand("sg").setExecutor(gameCmd);
    getCommand("sg").setTabCompleter(gameCmd);
  }

  private void loadSettings() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
    }
    File storageFile = new File(getDataFolder(), "games.yml");
    boolean copyDefaults = !storageFile.exists();
    this.settingsStorage = new GameStorage(storageFile, gameRegistry);

    try {
      if (copyDefaults) {
        GameSettingsRegistry settingsRegistry = settingsStorage.copyDefaults();
        settingsStorage.setLoadedSettings(settingsRegistry);
        log.warn("Configuration file '{}' does not exist. Creating new one", storageFile);
      } else {
        settingsStorage.loadSettings();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void setup(GameRegistry registry) {
    Registry<Listener> eventReg = RegistryFactory.defaultFactory().create("game_setup_events");
    for (String id : registry.keys()) {
      Game game = registry.getById(id);
      game.getGameSetup().registerListener(setupManager, eventReg);
    }

    for (Listener l : eventReg.values()) {
      getServer().getPluginManager().registerEvents(l, instance);
    }
  }
}
