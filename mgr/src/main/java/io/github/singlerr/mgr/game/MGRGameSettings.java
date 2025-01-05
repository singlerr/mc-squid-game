package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.bukkit.Location;

@Data
public final class MGRGameSettings implements GameSettings {

  private Map<Integer, Location> doors = new HashMap<>();
  private Map<Integer, Region> rooms = new HashMap<>();
  private Map<Integer, SoundSet> announcerSounds = new HashMap<>();
  private SoundSet musicSound = new SoundSet("mgr.music", 35f);
  private EntitySerializable pillarEntity;
  private Location pillarLocation;
  private float curtainDelay = 5f;
  private float curtainRadius = 15f;
  private float curtainMoveDistance = 1f;
  private float joiningRoomTime = 30f;

  public MGRGameSettings() {
    setupAnnouncerSounds();
  }

  private void setupAnnouncerSounds() {
    for (int i = 1; i <= 10; i++) {
      announcerSounds.put(i, new SoundSet("mgr.announce" + i, 10f));
    }
  }

  public void copy(MGRGameSettings o) {
    doors = new HashMap<>(o.doors);
    announcerSounds = new HashMap<>(o.announcerSounds);
    rooms = new HashMap<>(o.rooms);
    musicSound = o.musicSound;
    pillarEntity = o.pillarEntity;
    curtainDelay = o.curtainDelay;
    curtainMoveDistance = o.curtainMoveDistance;
    curtainRadius = o.curtainRadius;
    joiningRoomTime = o.joiningRoomTime;
    pillarLocation = o.pillarLocation;
  }
}
