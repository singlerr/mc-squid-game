package io.github.singlerr.intermediary.game;

import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import org.bukkit.event.Listener;

public final class IntermediaryGameSetup implements GameSetup<IntermediaryGameSettings> {

  private final IntermediaryGameSettings settings = new IntermediaryGameSettings();

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {

  }

  @Override
  public void setupStart(GameSetupContext<IntermediaryGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<IntermediaryGameSettings> context) {

  }

  @Override
  public Class<IntermediaryGameSettings> getType() {
    return IntermediaryGameSettings.class;
  }

  @Override
  public IntermediaryGameSettings getSettings(IntermediaryGameSettings data) {
    return settings;
  }

  @Override
  public GameSetupContext<IntermediaryGameSettings> createContext() {
    return new IntermediaryGameSetupContext(settings);
  }
}
