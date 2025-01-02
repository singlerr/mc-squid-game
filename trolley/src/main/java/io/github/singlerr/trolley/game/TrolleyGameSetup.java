package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.trolley.listener.TrolleyGameSetupListener;
import lombok.Getter;
import org.bukkit.event.Listener;

public final class TrolleyGameSetup implements GameSetup<TrolleyGameSettings> {

  private TrolleyGameSettings settings = new TrolleyGameSettings();
  @Getter
  private TrolleyGameSetupContext context = new TrolleyGameSetupContext(settings);

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {
    registry.register("trolley_setup_listener", new TrolleyGameSetupListener(this));
  }

  @Override
  public void setupStart(GameSetupContext<TrolleyGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<TrolleyGameSettings> context) {

  }

  @Override
  public Class<TrolleyGameSettings> getType() {
    return TrolleyGameSettings.class;
  }

  @Override
  public TrolleyGameSettings getSettings(TrolleyGameSettings data) {
    if (data != null) {
      settings.copy(data);
    }
    return settings;
  }

  @Override
  public GameSetupContext<TrolleyGameSettings> createContext() {
    return context;
  }
}
