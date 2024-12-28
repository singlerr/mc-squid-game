package io.github.singlerr.sg.core.setup;

import io.github.singlerr.sg.core.setup.helpers.RegionBuilder;
import lombok.Getter;

@Getter
public final class GameSetupHelper {

  private final RegionBuilder regionBuilder;

  public GameSetupHelper() {
    this.regionBuilder = new RegionBuilder();
  }
}
