package io.github.singlerr.admin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.singlerr.admin.commands.MagicWandCommand;
import io.github.singlerr.admin.network.PacketGameInfo;
import io.github.singlerr.admin.network.PacketPlayerInfoFragment;
import io.github.singlerr.admin.network.PacketRequestInfo;
import io.github.singlerr.admin.network.handler.PacketRequestInfoHandler;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketRegistry;
import io.github.singlerr.sg.core.utils.serializers.LocationTypeAdapter;
import io.github.singlerr.sg.core.utils.serializers.QuaternionfTypeAdapter;
import io.github.singlerr.sg.core.utils.serializers.Vector3fTypeAdapter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Slf4j
public final class Admin extends JavaPlugin {

  private static final Gson GSON = new GsonBuilder().setPrettyPrinting()
      .registerTypeAdapter(Location.class, new LocationTypeAdapter())
      .registerTypeAdapter(Vector3f.class, new Vector3fTypeAdapter())
      .registerTypeAdapter(Quaternionf.class, new QuaternionfTypeAdapter()).create();

  private File storageFile = new File(getDataFolder(), "entities.json");

  @Override
  public void onEnable() {
    try {
      setupConfig();
    } catch (IOException e) {
      log.error("Failed to load entities.json", e);
      getServer().getPluginManager().disablePlugin(this);
    }
    getServer().getPluginManager().registerEvents(new EventListener(), this);
    getCommand("mgw").setExecutor(new MagicWandCommand());
    registerPackets();
  }

  @Override
  public void onDisable() {
    try {
      copyDefaults(new ModelStorage(ModelTrackers.entriesNotNull()));
    } catch (IOException e) {
      log.error("Failed to save entities.json", e);
    }
  }

  private void registerPackets() {
    RegisteredServiceProvider<NetworkRegistry> network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class);
    if (network == null) {
      throw new RuntimeException("Failed to find network!");
    }

    network.getProvider().register(PacketGameInfo.ID,
        PacketRegistry.createRegistry(PacketGameInfo.class, PacketGameInfo::new, (pkt, p) -> {
        }));
    network.getProvider().register(PacketPlayerInfoFragment.ID,
        PacketRegistry.createRegistry(PacketPlayerInfoFragment.class, PacketPlayerInfoFragment::new,
            (pkt, p) -> {
            }));
    network.getProvider().register(PacketRequestInfo.ID,
        PacketRegistry.createRegistry(PacketRequestInfo.class, PacketRequestInfo::new,
            new PacketRequestInfoHandler()));
  }

  private void copyDefaults(ModelStorage storage) throws IOException {
    try (FileWriter writer = new FileWriter(storageFile)) {
      GSON.toJson(storage, writer);
    }
  }

  private void setupConfig() throws IOException {
    if (!getDataFolder().exists()) {
      getDataFolder().mkdir();
      copyDefaults(new ModelStorage(ModelTrackers.entriesNotNull()));
      return;
    }

    if (!storageFile.exists()) {
      copyDefaults(new ModelStorage(ModelTrackers.entriesNotNull()));
      return;
    }

    try (FileReader reader = new FileReader(storageFile)) {
      ModelStorage storage = GSON.fromJson(reader, ModelStorage.class);
      for (Map.Entry<UUID, EntityReference> e : storage.getEntities()
          .entrySet()) {
        ModelTrackers.addEntity(e.getKey(),
            EntityReference.of(e.getValue().getId(), e.getValue().getModelLocation(),
                e.getValue().getWorld(), e.getValue().getTransform()));
      }
    } catch (IOException ex) {
      copyDefaults(new ModelStorage(ModelTrackers.entriesNotNull()));
    }
  }


}
