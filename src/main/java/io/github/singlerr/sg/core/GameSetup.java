package io.github.singlerr.sg.core;

import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.setup.GameSetupContext;
import org.bukkit.event.Listener;

public interface GameSetup<T extends GameSettings> {

  void registerListener(GameSetupManager setupManager, Registry<Listener> registry);

  void setupStart(GameSetupContext<T> context);

  void setupEnd(GameSetupContext<T> context);

  Class<T> getType();

  T getSettings(T data);

  GameSetupContext<T> createContext();

}
