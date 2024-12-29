package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Region;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RLGLGameSettings implements GameSettings {

  private float killSwitch = 0.5f;
  private int time = 30 * 60;
  private Region deadRegion = new Region(null, null);

  private float startDelay = 3f;
  private float redLightDelay = 5.5f;
  private float greenLightDelay = 5.5f;

  private String startSound;
  private String redLightSound;
  private String greenLightSound;

  public void copyFrom(RLGLGameSettings settings) {
    this.killSwitch = settings.killSwitch;
    this.deadRegion = settings.deadRegion;
    this.startDelay = settings.startDelay;
    this.redLightDelay = settings.startDelay;
    this.greenLightDelay = settings.greenLightDelay;
    this.startSound = settings.startSound;
    this.greenLightSound = settings.greenLightSound;
    this.redLightSound = settings.redLightSound;
  }

}
