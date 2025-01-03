package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
public final class TrolleyGameSettings implements GameSettings {

  private Map<Integer, Train> trainEntities = new HashMap<>();
  private Map<Integer, Region> railways = new HashMap<>();

  private Region gameRegion;

  private float duration = 6f;
  private float slowedDuration = 1.5f;
  private float intermissionDuration = 1f;
  private float intermissionAngleRange = 90f;
  private float intermissionAngleAmount = 30f;

  private float idleDuration = 1.5f;
  private float killRadius = 3f;

  private float idleSpeed = 0.2f;
  private float slowedSpeed = 0.01f;

  private SoundSet trainSound = new SoundSet("trolley.train", 19f);

  public void copy(TrolleyGameSettings o) {
    trainEntities = new HashMap<>(o.trainEntities);
    railways = new HashMap<>(o.railways);
    gameRegion = o.gameRegion;
    duration = o.duration;
    slowedDuration = o.slowedDuration;
    intermissionDuration = o.intermissionDuration;
    idleDuration = o.idleDuration;
    killRadius = o.killRadius;
    intermissionAngleRange = o.intermissionAngleRange;
    intermissionAngleAmount = o.intermissionAngleAmount;
    idleSpeed = o.idleSpeed;
    slowedSpeed = o.slowedSpeed;
  }

}
