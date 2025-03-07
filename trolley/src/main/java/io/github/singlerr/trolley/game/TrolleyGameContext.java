package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.TaskScheduler;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import io.github.singlerr.trolley.network.PacketIntermissionRequest;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;

public final class TrolleyGameContext extends GameContext {

  private final SecureRandom random;
  private Map<Integer, Entity> trainEntities;
  private Map<Integer, TrainEntity> activeTracks;
  private Map<UUID, Ticker> statuses;
  @Getter
  private TaskScheduler scheduler;
  @Getter
  private TickableSoundPlayer soundPlayer;
  @Getter
  @Setter
  private TrolleyGameStatus gameStatus;

  public TrolleyGameContext(Map<UUID, GamePlayer> players,
                            GameStatus status,
                            GameEventBus eventBus,
                            GameSettings settings) {
    super(players, status, eventBus, settings);
    this.trainEntities = new HashMap<>();
    this.activeTracks = Collections.synchronizedMap(new HashMap<>());
    this.statuses = Collections.synchronizedMap(new HashMap<>());
    this.scheduler = new TaskScheduler();
    this.gameStatus = TrolleyGameStatus.IDLE;
    this.soundPlayer = new TickableSoundPlayer();
    this.random = new SecureRandom();
  }

  public TrolleyGameSettings getGameSettings() {
    return (TrolleyGameSettings) getSettings();
  }

  public void loadTrainEntities() {
    for (Map.Entry<Integer, Train> e : getGameSettings().getTrainEntities()
        .entrySet()) {
      int trainNum = e.getKey();
      Train t = e.getValue();
      World world = Bukkit.getWorld(t.getEntity().getWorld());
      if (world != null) {
        Entity entity = world.getEntity(t.getEntity().getId());
        if (entity != null) {
          for (EntitySerializable p : t.getEntity().getPassengers()) {
            Entity passenger = world.getEntity(p.getId());
            if (passenger instanceof Display display) {
              entity.addPassenger(display);
              display.setGravity(false);
              display.setInvulnerable(true);
              display.setShadowStrength(1f);
              display.setShadowRadius(1f);
              display.setTeleportDuration(0);
              display.setInterpolationDuration(0);
              display.setInterpolationDelay(0);
              Transformation transformation = display.getTransformation();
              transformation.getTranslation().set(0);
              display.setTransformation(transformation);
            }
          }
          entity.teleport(t.getInitialLocation());
          trainEntities.put(trainNum, entity);
        }
      }
    }
  }

  public void startIntermissions() {
    if (trainEntities.isEmpty()) {
      loadTrainEntities();
    }
    for (GamePlayer player : getPlayers(GameRole.TROY.getLevel())) {
      Ticker t = new Ticker(this, getGameSettings().getGameRegion(), player,
          getGameSettings().getIdleSpeed(),
          getGameSettings().getSlowedSpeed(),
          (long) (getGameSettings().getSlowedDuration() * 1000L),
          (long) ((getGameSettings().getIntermissionDuration() + 0.5f) * 1000L),
          (long) (getGameSettings().getIdleDuration() * 1000L));
      setPlayerStatus(player.getId(), t);
      t.start();
    }
  }

  public PacketIntermissionRequest createRequest() {
    float startAngle = randomAngle();
    return new PacketIntermissionRequest(
        (long) (getGameSettings().getIntermissionDuration() * 1000L), startAngle,
        getGameSettings().getIntermissionAngleAmount());
  }

  public void sendIntermissionRequest(Player player) {
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendTo(player, createRequest());
  }

  public float randomAngle() {
    int max = (int) (270 - getGameSettings().getIntermissionAngleAmount() -
        getGameSettings().getIntermissionAngleRange());
    return (float) random.nextInt(max) + getGameSettings().getIntermissionAngleRange();
  }

  public Entity getTrainEntity(int numTrack) {
    return trainEntities.get(numTrack);
  }

  public Ticker getPlayerStatus(UUID id) {
    return statuses.get(id);
  }

  public void setPlayerStatus(UUID id, Ticker status) {
    statuses.put(id, status);
  }

  public void removePlayerStatus(UUID id) {
    statuses.remove(id);
  }

  private void runTrain(int trackNum) {
    TrainEntity train = new TrainEntity(this, getGameSettings().getTrainEntities().get(trackNum),
        trainEntities.get(trackNum),
        (long) (getGameSettings().getDuration() * 1000L), getGameSettings().getKillRadius(),
        getGameSettings().getRailways().get(trackNum));
    train.init();
    activeTracks.put(trackNum, train);
  }

  public boolean fireTrain(int trackNum) {
    if (activeTracks.containsKey(trackNum)) {
      return false;
    }

    soundPlayer.enqueue(getPlayers(),
        getGameSettings().getTrainSound().getSound(),
        getGameSettings().getTrainSound().getDuration(), () -> {
          runTrain(trackNum);
        });

    return true;
  }

  public void tickTrains(long time) {
    for (Integer numTrack : activeTracks.keySet()) {
      TrainEntity train = activeTracks.get(numTrack);
      if (train.end()) {
        train.reset();
        activeTracks.remove(numTrack);
        continue;
      }
      train.tick(time);
    }
  }

  public void tickPlayers(long time) {
    for (Ticker value : statuses.values()) {
      value.tick(time);
    }
  }

  public void resetTrain(int num) {
    Entity e = trainEntities.get(num);
    Train t = getGameSettings().getTrainEntities().get(num);
    if (e != null && t != null) {
      e.teleport(t.getInitialLocation());
    }
  }

}
