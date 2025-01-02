package io.github.singlerr.roulette.game;

import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import org.bukkit.event.Listener;

public final class RouletteGameSetup implements GameSetup<RouletteGameSettings> {

  private RouletteGameSettings settings = new RouletteGameSettings();

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {

  }

  @Override
  public void setupStart(GameSetupContext<RouletteGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<RouletteGameSettings> context) {

  }

  @Override
  public Class<RouletteGameSettings> getType() {
    return RouletteGameSettings.class;
  }

  @Override
  public RouletteGameSettings getSettings(RouletteGameSettings data) {
    if (data != null) {
      settings.copy(data);
    }
    return settings;
  }

  @Override
  public GameSetupContext<RouletteGameSettings> createContext() {
    return new RouletteGameSetupContext(settings);
  }
}
