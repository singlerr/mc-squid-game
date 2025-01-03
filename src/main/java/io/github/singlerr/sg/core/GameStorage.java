package io.github.singlerr.sg.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.singlerr.sg.core.registry.impl.DefaultGameSettingsRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.serializers.LocationTypeAdapter;
import io.github.singlerr.sg.core.utils.serializers.QuaternionfTypeAdapter;
import io.github.singlerr.sg.core.utils.serializers.Vector3fTypeAdapter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Slf4j
public final class GameStorage {

  private final Gson gson;
  private final File file;
  private final GameRegistry games;
  @Getter
  @Setter
  private GameSettingsRegistry loadedSettings;

  public GameStorage(File file, GameRegistry games) {
    this.file = file;
    this.games = games;
    this.gson = new GsonBuilder().setPrettyPrinting()
        .registerTypeAdapter(Location.class, new LocationTypeAdapter())
        .registerTypeAdapter(Vector3f.class, new Vector3fTypeAdapter())
        .registerTypeAdapter(Quaternionf.class, new QuaternionfTypeAdapter()).create();
  }

  public GameSettingsRegistry loadSettings() throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException("File " + file.getName() + " does not exist");
    }

    JsonObject obj = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
    JsonArray gameList = obj.getAsJsonArray("games");

    if (gameList == null) {
      log.warn(
          "Malformed JSON! A config must contain one 'games' key to denote list of game settings. Copying defaults...");
      return (loadedSettings = copyDefaults());
    }
    GameSettingsRegistry registry = DefaultGameSettingsRegistry.create();
    for (JsonElement g : gameList) {
      JsonObject gameObject = g.getAsJsonObject();

      if (!gameObject.has("id")) {
        log.info(
            "Malformed JSON! Each game section must have a id string that denotes game id! Skipping...");
        continue;
      }

      String id = gameObject.get("id").getAsString();
      if (id == null) {
        log.info("Malformed JSON! A id must be String! Skipping...");
        continue;
      }

      Game game = games.getById(id);
      if (game == null) {
        log.error("Settings for {} exists but game with {} does not. Skipping...", id, id);
        continue;
      }

      if (!gameObject.has("type")) {
        log.info(
            "Malformed JSON! Each game section must have a type string that denotes class name! Skipping...");
        continue;
      }

      String typeClass = gameObject.get("type").getAsString();
      if (typeClass == null) {
        log.info("Malformed JSON! A type must be String! Skipping...");
        continue;
      }
      Class<?> settingsCls = null;
      try {
        settingsCls = Class.forName(typeClass);
      } catch (ClassNotFoundException e) {
        log.error("Could not find class with name {}. Skipping...", typeClass);
        continue;
      }

      if (!gameObject.has("impl")) {
        log.warn(
            "{} does not have actual implementation! All will be set to defaults when server shutting down.",
            typeClass);
        registry.register(id, game.getGameSetup().getSettings(null));
        continue;
      }

      GameSettings settings =
          gson.fromJson(gameObject.get("impl"), (Class<? extends GameSettings>) settingsCls);
      if (settings == null) {
        log.error("Failed to load settings for {}. Defaults will be set.", id);
        registry.register(id, game.getGameSetup().getSettings(null));
        continue;
      }
      registry.register(id, settings);
      log.info("Successfully loaded settings for {}.", id);
    }

    this.loadedSettings = registry;
    return registry;
  }

  private JsonObject serialize(GameSettingsRegistry registry) {
    JsonObject root = new JsonObject();
    JsonArray gameList = new JsonArray();
    for (String key : registry.keys()) {
      GameSettings settings = registry.getById(key);
      JsonObject gameObject = new JsonObject();
      gameObject.addProperty("id", key);
      gameObject.addProperty("type", settings.getClass().getName());
      gameObject.add("impl", gson.toJsonTree(settings));
      gameList.add(gameObject);
    }

    root.add("games", gameList);
    return root;
  }

  public void save() throws IOException {
    GameSettingsRegistry registry = DefaultGameSettingsRegistry.create();
    if (games.values().isEmpty()) {
      return;
    }

    for (String key : games.keys()) {
      Game game = games.getById(key);
      GameSettings settings = game.getGameSetup().getSettings(null);
      registry.register(key, settings);
    }
    JsonObject obj = serialize(registry);
    try (FileWriter writer = new FileWriter(file)) {
      gson.toJson(obj, writer);
    }
  }

  public GameSettingsRegistry copyDefaults() throws IOException {
    GameSettingsRegistry registry = DefaultGameSettingsRegistry.create();
    if (games.values().isEmpty()) {
      return registry;
    }

    for (String key : games.keys()) {
      Game game = games.getById(key);
      GameSettings settings = game.getGameSetup().getSettings(null);
      registry.register(key, settings);
    }
    return registry;
  }
}
