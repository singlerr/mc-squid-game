package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RLGLGameSettings implements GameSettings {

  private float killMovementThreshold = 0.5f;


  public void copyFrom(RLGLGameSettings settings) {
    this.killMovementThreshold = settings.killMovementThreshold;
  }

}
