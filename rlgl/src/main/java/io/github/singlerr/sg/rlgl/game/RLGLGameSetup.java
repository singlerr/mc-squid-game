package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.GameSetup;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.rlgl.listener.RLGLGameSetupListener;
import lombok.Getter;
import org.bukkit.event.Listener;

public class RLGLGameSetup implements GameSetup<RLGLGameSettings> {

  @Getter
  private final RLGLGameSettings settings = new RLGLGameSettings();
  private final RLGLGameSetupContext context = new RLGLGameSetupContext(settings);

  @Override
  public void registerListener(GameSetupManager setupManager, Registry<Listener> registry) {
    registry.register("rlgl_setup", new RLGLGameSetupListener(setupManager, context));
  }

  @Override
  public void setupStart(GameSetupContext<RLGLGameSettings> context) {

  }

  @Override
  public void setupEnd(GameSetupContext<RLGLGameSettings> context) {

  }

  @Override
  public Class<RLGLGameSettings> getType() {
    return RLGLGameSettings.class;
  }

  @Override
  public RLGLGameSettings getSettings(RLGLGameSettings data) {
    if (data == null) {
      return settings;
    }

    settings.copyFrom(data);
    return settings;
  }

  @Override
  public GameSetupContext<RLGLGameSettings> createContext() {
    return context;
  }
}
