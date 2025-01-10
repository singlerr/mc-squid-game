package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.Transform;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Data
@NoArgsConstructor
public class RLGLGameSettings implements GameSettings {

  private float killSwitch = 0.1f;
  private int time = 5 * 60;
  private Region deadRegion = new Region(null, null);

  private float startDelay = 1f;
  private float greenLightTurnDelay = 3f;
  private float redLightTurnDelay = 3f;

  private SoundSet soundOne = new SoundSet("rlgl.preset1", 4.0f);
  private SoundSet soundTwo = new SoundSet("rlgl.preset2", 5.0f);
  private SoundSet soundThree = new SoundSet("rlgl.preset3", 5.0f);
  private SoundSet soundFour = new SoundSet("rlgl.preset4", 6.0f);
  private SoundSet soundFive = new SoundSet("rlgl.preset5", 8.0f);
  private SoundSet transitionSound = new SoundSet("rlgl.transition", 5f);

  private float playerSpeed = 0.1f;

  private Transform backState =
      new Transform(null, new Quaternionf().rotationXYZ(0, 0, Mth.PI), new Vector3f(10));
  private Transform frontState =
      new Transform(null, new Quaternionf().rotationXYZ(0, 0, 0), new Vector3f(10));

  private String startSound = "rlgl.broadcast_start_";
  private EntitySerializable youngHee = new EntitySerializable();
  private Quaternionf originalRot = new Quaternionf();

  private float scaleX = 1.0f;
  private float scaleY = 1.0f;
  private float scaleZ = 1.0f;

  private int nodeIndex = 12;
  private String modelLocation = "vanilla-gltf:models/younghee.glb";

  public void copyFrom(RLGLGameSettings settings) {
    this.killSwitch = settings.killSwitch;
    this.deadRegion = settings.deadRegion;
    this.greenLightTurnDelay = settings.greenLightTurnDelay;
    this.redLightTurnDelay = settings.redLightTurnDelay;
    this.startDelay = settings.startDelay;
    this.soundOne = settings.soundOne;
    this.soundTwo = settings.soundTwo;
    this.soundThree = settings.soundThree;
    this.soundFour = settings.soundFour;
    this.soundFive = settings.soundFive;
    this.startSound = settings.startSound;
    this.transitionSound = settings.transitionSound;
    this.playerSpeed = settings.playerSpeed;
    this.youngHee = settings.youngHee;
    this.scaleX = settings.scaleX;
    this.scaleY = settings.scaleY;
    this.scaleZ = settings.scaleZ;
    this.nodeIndex = settings.nodeIndex;
    this.modelLocation = settings.modelLocation;
  }

}
