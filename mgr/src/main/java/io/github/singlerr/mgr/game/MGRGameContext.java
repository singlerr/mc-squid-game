package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Interpolator;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.TaskScheduler;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.type.Door;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

@Slf4j
@Getter
@Setter
public final class MGRGameContext extends GameContext {

  private MGRGameStatus gameStatus;

  private TaskScheduler scheduler;

  private TickableSoundPlayer soundPlayer;

  private long joiningStartedTime;

  private Entity pillar;

  private Interpolator interpolator;

  private int playerCount;

  private SecureRandom random;

  private Set<Location> barrierCache;

  public MGRGameContext(List<GamePlayer> players,
                        GameStatus status,
                        GameEventBus eventBus,
                        GameSettings settings) {
    super(players, status, eventBus, settings);
    this.soundPlayer = new TickableSoundPlayer();
    this.scheduler = new TaskScheduler();
    this.random = new SecureRandom();
    this.barrierCache = new HashSet<>();
  }

  public MGRGameSettings getGameSettings() {
    return (MGRGameSettings) getSettings();
  }

  public void startNewSession(int playerCount) {
    gameStatus = MGRGameStatus.PLAYING_MUSIC;
    this.playerCount = playerCount;
    int offset = random.nextInt(2) - 1;
    float distPerTick = getGameSettings().getCurtainMoveDistance() /
        (getGameSettings().getCurtainDelay() * 1000);
    setBarrier();
    interpolator = new Interpolator((long) (getGameSettings().getCurtainDelay() * 1000), p -> {
      if (pillar.getLocation().distance(getGameSettings().getPillarLocation()) < 1) {
        return;
      }
      pillar.setVelocity(new Vector(0, -distPerTick, 0));
    });
    scheduler.enqueue((long) (getGameSettings().getCurtainDelay() * 1000), () -> {
      SoundSet music = getGameSettings().getMusicSound();
      long dur = (long) ((music.getDuration() - offset) * 1000L);
      this.soundPlayer.enqueue(getPlayers(), music.getSound(), dur, () -> {
        Bukkit.getServer().stopSound(SoundStop.named(Key.key(music.getSound())));
        openCurtain();
      });
    });
  }

  public void openCurtain() {
    gameStatus = MGRGameStatus.OPENING_CURTAIN;
    float distPerTick = getGameSettings().getCurtainMoveDistance() /
        (getGameSettings().getCurtainDelay() * 1000);
    interpolator = new Interpolator((long) (getGameSettings().getCurtainDelay() * 1000), p -> {
      pillar.setVelocity(new Vector(0, distPerTick, 0));
    }); // move curtain entity upwards
    scheduler.enqueue((long) (getGameSettings().getCurtainDelay() * 1000L), () -> {
      SoundSet set = getGameSettings().getAnnouncerSounds().get(playerCount);
      if (set == null) {
        log.error("No announcer sounds available for {} player counts.", playerCount);
        return;
      }
      startJoiningRoom();
      this.soundPlayer.enqueue(getPlayers(), set.getSound(), (long) (set.getDuration() * 1000L),
          this::startClosingRoom);
    });
  }

  public void startJoiningRoom() {
    gameStatus = MGRGameStatus.JOINING_ROOM;
    joiningStartedTime = System.currentTimeMillis();
    removeBarrier();
  }

  public void startClosingRoom() {
    gameStatus = MGRGameStatus.CLOSING_ROOM;
    for (Location loc : getGameSettings().getDoors().values()) {
      if (loc.getBlock().getState() instanceof Door d) {
        d.setOpen(false);
      }
    }
    for (Map.Entry<Integer, Region> entry : ((MGRGameSettings) getSettings()).getRooms()
        .entrySet()) {
      int roomNum = entry.getKey();
      Region roomRegion = entry.getValue();
      int count = getRoomPlayerCount(roomRegion);
      if (playerCount != count) {
        broadcast(Component.text(roomNum + "번 인원 수 미충족, 현재 인원: " + count).style(Style.style(
            NamedTextColor.YELLOW)), GameRole.ADMIN);
      }
    }
  }

  public void closeSession() {
    float distPerTick = getGameSettings().getCurtainMoveDistance() /
        (getGameSettings().getCurtainDelay() * 1000);
    interpolator = new Interpolator((long) (getGameSettings().getCurtainDelay() * 1000), p -> {
      if (pillar.getLocation().distance(getGameSettings().getPillarLocation()) < 1) {
        return;
      }
      pillar.setVelocity(new Vector(0, -distPerTick, 0));
    });
    scheduler.enqueue((long) (getGameSettings().getCurtainDelay() * 1000), () -> {
      gameStatus = MGRGameStatus.IDLE;
      setBarrier();
    });
  }

  public void setBarrier() {
    if (barrierCache.isEmpty()) {
      barrierCache = createBarrier(getPillar().getLocation(),
          (int) getGameSettings().getCurtainMoveDistance());
    }
    for (Location l : barrierCache) {
      l.getBlock().setType(Material.BARRIER);
    }
  }

  public void removeBarrier() {
    if (barrierCache.isEmpty()) {
      barrierCache = createBarrier(getPillar().getLocation(),
          (int) getGameSettings().getCurtainMoveDistance());
    }
    for (Location l : barrierCache) {
      l.getBlock().setType(Material.AIR);
    }
  }

  private Set<Location> createBarrier(Location center, int height) {
    Set<Location> lower = new HashSet<>();
    for (int i = 0; i < 360; i++) {
      float angle = (float) Math.toRadians(i);
      float x = center.getBlockX() + (float) Math.cos(angle);
      float y = center.getBlockY();
      float z = center.getBlockZ() + (float) Math.sin(angle);

      lower.add(new Location(center.getWorld(), x, y, z));
    }

    Set<Location> med = new HashSet<>();
    for (Location location : lower) {
      for (int i = 0; i < height; i++) {
        med.add(new Location(location.getWorld(), location.getX(), location.getY() + i,
            location.getZ()));
      }
    }

    lower.addAll(med);
    return lower;
  }

  public void setBarrier(Set<Location> locations, Material type) {
    locations.forEach(l -> l.getBlock().setType(type));
  }

  private int getRoomPlayerCount(Region region) {
    World world = region.getStart().getWorld();
    BoundingBox range =
        new BoundingBox(region.getStart().x(), region.getStart().y(), region.getStart().z(),
            region.getEnd().x(), region.getEnd().y(), region.getEnd().z());
    return world.getNearbyEntities(range, entity -> {
      if (!(entity instanceof Player player)) {
        return false;
      }
      GamePlayer gamePlayer = getPlayer(player.getUniqueId());
      if (gamePlayer == null) {
        return false;
      }
      return gamePlayer.getRole() == GameRole.USER;
    }).size();
  }
}
