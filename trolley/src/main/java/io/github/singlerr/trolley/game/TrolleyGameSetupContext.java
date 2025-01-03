package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.setup.helpers.RegionBuilder;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.Region;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import org.bukkit.entity.Entity;

public final class TrolleyGameSetupContext extends GameSetupContext<TrolleyGameSettings> {

  @Getter
  private final Map<UUID, Track> trackBuilders;

  private final Map<UUID, RegionBuilder> gameRegionBuilders;


  public TrolleyGameSetupContext(TrolleyGameSettings settings) {
    super(settings);
    this.trackBuilders = new HashMap<>();
    this.gameRegionBuilders = new HashMap<>();
  }

  public Integer getTrackNumber(UUID id) {
    if (!trackBuilders.containsKey(id)) {
      return null;
    }
    return trackBuilders.get(id).trackNumber();
  }

  public void beginGameRegion(UUID id) {
    gameRegionBuilders.put(id, new RegionBuilder());
  }


  public void beginTrack(UUID id, int trackNum) {
    trackBuilders.put(id, new Track(trackNum, new RegionBuilder()));
  }

  public void endTrack(UUID id) {
    trackBuilders.remove(id);
  }

  public void endGameRegion(UUID id) {
    gameRegionBuilders.remove(id);
  }

  public RegionBuilder getGameRegionBuilder(UUID id) {
    return gameRegionBuilders.get(id);
  }

  public RegionBuilder getRegionBuilder(UUID id) {
    if (!trackBuilders.containsKey(id)) {
      return null;
    }
    return trackBuilders.get(id).builder();
  }

  public void setGameRegion(Region region) {
    getSettings().setGameRegion(region);
  }

  public void setTrainTrack(int trainNum, Region region) {
    getSettings().getRailways().put(trainNum, region);
  }

  public void setTrainEntity(int trainNum, Entity entity) {
    getSettings().getTrainEntities().put(trainNum,
        new Train(entity.getLocation(), EntitySerializable.of(entity)));
  }

  private record Track(int trackNumber, RegionBuilder builder) {
  }
}
