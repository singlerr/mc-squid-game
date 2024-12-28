package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.registry.impl.DefaultGameSettingsRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.ReflectionUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

@Slf4j
public final class GameStorage {

  private final File file;
  private final GameRegistry games;
  @Getter
  @Setter
  private GameSettingsRegistry loadedSettings;

  public GameStorage(File file, GameRegistry games) {
    this.file = file;
    this.games = games;
  }

  public GameSettingsRegistry loadSettings() throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException("File " + file.getName() + " does not exist");
    }
    YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

    if (!config.contains("games")) {
      throw new IllegalStateException(
          "Malformed YAML! A config must contain one 'games' key to denote list of game settings");
    }

    ConfigurationSection gameSection = config.getConfigurationSection("games");
    if (gameSection == null) {
      throw new IllegalStateException("Malformed YAML! A node 'games' must be a section");
    }

    GameSettingsRegistry registry = DefaultGameSettingsRegistry.create();
    for (String gameId : gameSection.getKeys(false)) {
      Game game = games.getById(gameId);
      if (game == null) {
        log.warn("Game with id '{}' does not exist. Skipping", gameId);
        continue;
      }

      GameSetup<? extends GameSettings> gameSetup = game.getGameSetup();
      GameSettings settings = gameSection.getObject(gameId, gameSetup.getType());
      if (settings == null) {
        settings = ReflectionUtils.expectEmptyConstructor(gameSetup.getType());
        if (settings == null) {
          throw new IllegalStateException("Settings initialization was failed");
        }
      }

      registry.register(gameId, settings);
    }

    this.loadedSettings = registry;
    return registry;
  }

  public GameSettingsRegistry copyDefaults() throws IOException {
    YamlConfiguration config = new YamlConfiguration();
    GameSettingsRegistry registry = DefaultGameSettingsRegistry.create();
    if (registry.values().isEmpty()) {
      config.set("games", new MemoryConfiguration());
      config.save(file);
      return registry;
    }

    for (String key : games.keys()) {
      Game game = games.getById(key);
      GameSettings settings = game.getGameSetup().getSettings(null);

      config.set("games." + key, settings);
      registry.register(key, settings);
    }

    config.save(file);
    return registry;
  }


}
