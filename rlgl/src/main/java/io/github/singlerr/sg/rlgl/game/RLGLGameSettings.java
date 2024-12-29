package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RLGLGameSettings implements GameSettings {

  private float killSwitch = 0.5f;
  private int time = 30 * 60;
  private Region deadRegion = new Region(null, null);

  private float startDelay = 3f;
  private float greenLightDelay = 1f;

  private SoundSet soundOne = new SoundSet("game1.preset1", 2.0f);
  private SoundSet soundTwo = new SoundSet("game1.preset2", 3.0f);
  private SoundSet soundThree = new SoundSet("game1.preset3", 4.0f);
  private SoundSet soundFour = new SoundSet("game1.preset4", 5.0f);
  private SoundSet soundFive = new SoundSet("game1.preset5", 8.0f);


  private String startSound = "broadcast.start";
  private String youngHeeWorld;
  private UUID youngHeeId;

  private float scaleX = 1.0f;
  private float scaleY = 1.0f;
  private float scaleZ = 1.0f;

  private int nodeIndex = -1;
  private String modelLocation = "vanilla-gltf:models/younghee.glb";

  public void copyFrom(RLGLGameSettings settings) {
    this.killSwitch = settings.killSwitch;
    this.deadRegion = settings.deadRegion;
    this.startDelay = settings.startDelay;
    this.greenLightDelay = settings.greenLightDelay;
    this.soundOne = settings.soundOne;
    this.soundTwo = settings.soundTwo;
    this.soundThree = settings.soundThree;
    this.soundFour = settings.soundFour;
    this.soundFive = settings.soundFive;
    this.startSound = settings.startSound;
    this.youngHeeId = settings.youngHeeId;
    this.youngHeeWorld = settings.youngHeeWorld;
    this.scaleX = settings.scaleX;
    this.scaleY = settings.scaleY;
    this.scaleZ = settings.scaleZ;
    this.nodeIndex = settings.nodeIndex;
    this.modelLocation = settings.modelLocation;
  }

}
