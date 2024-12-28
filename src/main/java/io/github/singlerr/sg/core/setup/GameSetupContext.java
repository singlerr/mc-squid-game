package io.github.singlerr.sg.core.setup;

import lombok.Getter;

public abstract class GameSetupContext<T extends GameSettings> {

  @Getter
  private final GameSetupHelper setupHelper;

  @Getter
  private final T settings;

  public GameSetupContext(T settings) {
    this.setupHelper = new GameSetupHelper();
    this.settings = settings;
  }


}
