package io.github.singlerr.sg.core.registry.impl;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameRegistry;

public final class DefaultGameRegistry extends MappedRegistry<Game> implements GameRegistry {

  private DefaultGameRegistry() {
  }

  public static GameRegistry create() {
    return new DefaultGameRegistry();
  }

  @Override
  public String getId() {
    return "game_registry";
  }
}
