package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.commands.GameCommands;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketRegistry;
import io.github.singlerr.sg.core.network.handler.PacketRequestSyncHandler;
import io.github.singlerr.sg.core.network.impl.PluginAwareNetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketAnimateTransformationModel;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.network.packets.PacketRequestSync;
import io.github.singlerr.sg.core.network.packets.PacketTransformModel;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.registry.impl.DefaultGameRegistry;
import io.github.singlerr.sg.core.registry.impl.RegistryFactory;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@Slf4j
public final class GameCore extends JavaPlugin {

  @Getter
  private static GameCore instance;
  private GameStorage settingsStorage;

  private NetworkRegistry networkRegistry;
  private GameRegistry gameRegistry;
  @Getter
  private GameLifecycle coreLifecycle;
  private GameSetupManager setupManager;

  @Accessors(fluent = true)
  @Getter
  @Setter
  private boolean shouldBan = true;

  public GameCore() {
    GameCore.instance = this;
  }

  @Override
  public void onEnable() {
    log.info("Waiting for all other plugins fully loaded");
    Watcher watcher = new Watcher(this::allPluginsEnabled, this::load);
    watcher.runTaskTimer(instance, 0L, 1L);

  }

  private boolean allPluginsEnabled() {
    return Arrays.stream(getServer().getPluginManager().getPlugins())
        .allMatch(p -> getServer().getPluginManager().isPluginEnabled(p));
  }

  private void load() {
    this.setupManager = new GameSetupManager(gameRegistry);
    setup(gameRegistry);
    loadSettings();
    log.info("Loaded following games: {}", gameRegistry.keys());
    this.coreLifecycle =
        new GameLifecycle(gameRegistry, settingsStorage.getLoadedSettings(), instance);
    this.coreLifecycle.runTaskTimer(instance, 0L, 1L);
    registerPackets(networkRegistry);
    ((PluginAwareNetworkRegistry) networkRegistry).registerToMessengers();
    registerCommands();
  }

  @Override
  public void onDisable() {
    try {
      if (!getDataFolder().exists()) {
        getDataFolder().mkdir();
      }
      settingsStorage.save();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
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

  private void registerPackets(NetworkRegistry registry) {
    registry.register(PacketAnimateTransformationModel.ID, PacketRegistry.createRegistry(
        PacketAnimateTransformationModel.class, PacketAnimateTransformationModel::new, (pkt, p) -> {
        })); // only one direction
    registry.register(PacketTransformModel.ID,
        PacketRegistry.createRegistry(PacketTransformModel.class, PacketTransformModel::new,
            (pkt, p) -> {
            }));// only one direction
    registry.register(PacketInitModel.ID,
        PacketRegistry.createRegistry(PacketInitModel.class, PacketInitModel::new, (pkt, p) -> {
        }));// only one direction
    registry.register(PacketRequestSync.ID,
        PacketRegistry.createRegistry(PacketRequestSync.class, PacketRequestSync::new,
            new PacketRequestSyncHandler(instance)));
  }

  private void registerCommands() {
    GameCommands gameCmd =
        new GameCommands(coreLifecycle, gameRegistry, setupManager, settingsStorage);
    getCommand("sg").setExecutor(gameCmd);
    getCommand("sg").setTabCompleter(gameCmd);
  }

  private void loadSettings() {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
      saveDefaultConfig();
      reloadConfig();
    }
    saveDefaultConfig();
    reloadConfig();

    File storageFile = new File(getDataFolder(), "games.json");
    boolean copyDefaults = !storageFile.exists();
    this.settingsStorage = new GameStorage(storageFile, gameRegistry);

    try {
      if (copyDefaults) {
        GameSettingsRegistry settingsRegistry = settingsStorage.copyDefaults();
        settingsStorage.setLoadedSettings(settingsRegistry);
        settingsStorage.save();
        log.warn("Configuration file '{}' does not exist. Creating new one", storageFile);
      } else {
        settingsStorage.loadSettings();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    for (String id : gameRegistry.keys()) {
      Game g = gameRegistry.getById(id);
      GameSettings s = settingsStorage.getLoadedSettings().getById(id);
      g.getGameSetup().getSettings(s);
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

  public String getAdminSkinUrl() {
    return getConfig().getString("admin_skin_url");
  }

  public List<String> getPlayerSkinUrl(boolean male) {
    return getConfig().getStringList(male ? "player_skin_url_man" : "player_skin_url_woman");
  }

  private static class Watcher extends BukkitRunnable {

    private final Supplier<Boolean> predicate;
    private final Runnable callback;

    private Watcher(Supplier<Boolean> predicate, Runnable callback) {
      this.predicate = predicate;
      this.callback = callback;
    }

    @Override
    public void run() {
      if (predicate.get()) {
        cancel();
        callback.run();
        return;
      }
    }
  }
}
