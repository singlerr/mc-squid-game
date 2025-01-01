package io.github.singlerr.mgr.game;

import io.github.singlerr.mgr.listener.MGRGameSetupListener;
import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import lombok.Getter;
import org.bukkit.event.Listener;

public final class MGRGameSetup implements GameSetup<MGRGameSettings> {

  private MGRGameSettings settings = new MGRGameSettings();
  @Getter
  private MGRGameSetupContext context = new MGRGameSetupContext(settings);

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {
    registry.register("mgr_setup_listener", new MGRGameSetupListener(setupManager, context));
  }

  @Override
  public void setupStart(GameSetupContext<MGRGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<MGRGameSettings> context) {

  }

  @Override
  public Class<MGRGameSettings> getType() {
    return MGRGameSettings.class;
  }

  @Override
  public MGRGameSettings getSettings(MGRGameSettings data) {
    if (data == null) {
      return settings;
    }
    settings.copy(data);
    return settings;
  }

  @Override
  public GameSetupContext<MGRGameSettings> createContext() {
    return context;
  }
}
