package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.setup.GameSetupContext;
import io.github.singlerr.sg.core.setup.helpers.RegionBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public final class MGRGameSetupContext extends GameSetupContext<MGRGameSettings> {

  private final Map<UUID, Consumer<Entity>> pillarSetupContexts;
  private final Map<UUID, Consumer<Location>> doorSetupContexts;
  private final Map<UUID, RegionSelector> regionSelectors;

  public MGRGameSetupContext(MGRGameSettings settings) {
    super(settings);
    this.pillarSetupContexts = new HashMap<>();
    this.doorSetupContexts = new HashMap<>();
    this.regionSelectors = new HashMap<>();
  }

  public void beginSelector(UUID id, int roomNum) {
    this.regionSelectors.put(id, new RegionSelector(roomNum, new RegionBuilder()));
  }

  public RegionSelector getSelector(UUID id) {
    return regionSelectors.get(id);
  }

  public void endSelector(UUID id) {
    regionSelectors.remove(id);
  }

  public Consumer<Entity> getPillarSetupContext(UUID id) {
    return pillarSetupContexts.get(id);
  }

  public void beginPillarSetup(UUID id, Consumer<Entity> executor) {
    this.pillarSetupContexts.put(id, executor);
  }

  public void endPillarSetup(UUID id) {
    this.pillarSetupContexts.remove(id);
  }

  public void beginDoorSetup(UUID id, Consumer<Location> executor) {
    this.doorSetupContexts.put(id, executor);
  }

  public Consumer<Location> getDoorSetupContext(UUID id) {
    return this.doorSetupContexts.get(id);
  }

  public void endDoorSetup(UUID id) {
    this.doorSetupContexts.remove(id);
  }

  @Data
  @AllArgsConstructor
  public static class RegionSelector {

    private int roomNum;
    private RegionBuilder builder;

  }
}
