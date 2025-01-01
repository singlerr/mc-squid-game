package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import org.bukkit.event.Listener;

public final class DalgonaGameSetup implements GameSetup<DalgonaGameSettings> {

  private DalgonaGameSettings settings = new DalgonaGameSettings();
  private DalgonaGameSetupContext setupContext = new DalgonaGameSetupContext(settings);

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {

  }

  @Override
  public void setupStart(GameSetupContext<DalgonaGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<DalgonaGameSettings> context) {

  }

  @Override
  public Class<DalgonaGameSettings> getType() {
    return DalgonaGameSettings.class;
  }

  @Override
  public DalgonaGameSettings getSettings(DalgonaGameSettings data) {
    if (data != null) {
      settings.copy(data);
    }
    return settings;
  }

  @Override
  public GameSetupContext<DalgonaGameSettings> createContext() {
    return setupContext;
  }
}
