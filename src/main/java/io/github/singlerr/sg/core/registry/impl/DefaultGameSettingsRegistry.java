package io.github.singlerr.sg.core.registry.impl;

import io.github.singlerr.sg.core.GameSettingsRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;

public final class DefaultGameSettingsRegistry extends MappedRegistry<GameSettings> implements
    GameSettingsRegistry {

  private DefaultGameSettingsRegistry() {
  }

  public static DefaultGameSettingsRegistry create() {
    return new DefaultGameSettingsRegistry();
  }

  @Override
  public String getId() {
    return "game_settings";
  }
}
