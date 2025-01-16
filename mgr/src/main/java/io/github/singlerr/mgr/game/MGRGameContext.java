package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Interpolator;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.TaskScheduler;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

@Slf4j
@Getter
@Setter
public final class MGRGameContext extends GameContext {

  private final Interpolator interpolator;
  private final ChatColor glowingColor;
  private MGRGameStatus gameStatus;
  private TaskScheduler scheduler;
  private TickableSoundPlayer soundPlayer;
  private long joiningStartedTime;
  private Entity pillar;
  private int playerCount;
  private SecureRandom random;
  private Set<Location> barrierCache;
  private Vector3f initialPos;
  private Map<Integer, AtomicInteger> playerCounts;
  private Map<UUID, Mount> mountList;
  @Accessors(fluent = true)
  private boolean rotatePlayer;
  @Accessors(fluent = true)
  private boolean lockPlayer;


  public MGRGameContext(Map<UUID, GamePlayer> players,
                        GameStatus status,
                        GameEventBus eventBus,
                        GameSettings settings) {
    super(players, status, eventBus, settings);
    this.soundPlayer = new TickableSoundPlayer();
    this.scheduler = new TaskScheduler();
    this.random = new SecureRandom();
    this.barrierCache = new HashSet<>();
    this.interpolator = new Interpolator();
    this.playerCounts = Collections.synchronizedMap(new HashMap<>());
    this.mountList = Collections.synchronizedMap(new HashMap<>());
    this.rotatePlayer = false;
    this.lockPlayer = false;
    this.glowingColor = ChatColor.AQUA;
  }

  public Display getDisplay(Entity entity) {
    return entity.getPassengers().stream().filter(e -> e instanceof Display).map(e -> (Display) e)
        .findAny().orElse(null);
  }

  public MGRGameSettings getGameSettings() {
    return (MGRGameSettings) getSettings();
  }

  private void move(Display display, Vector3f v, int dur) {
    Transformation t = display.getTransformation();
    t.getTranslation().add(v);
    display.setInterpolationDelay(0);
    display.setInterpolationDuration(dur);
    display.setTransformation(t);
  }

  public void startNewSession(int playerCount) {
    // setup player counting system per rooms
    playerCounts.clear();
    for (Integer i : getGameSettings().getRooms().keySet()) {
      playerCounts.put(i, new AtomicInteger(0));
    }

    PlayerUtils.disableGlowing(getOnlinePlayers(GameRole.TROY.getLevel()),
        getOnlinePlayers(GameRole.ADMIN));
    // setup mounts
//    float rotationSpeed = 0.01f;
//    setupMounts(rotationSpeed);

    //lock player
    teleportToCenter();
    lockPlayer(true);

    // set game status
    gameStatus = MGRGameStatus.PLAYING_MUSIC;
    this.playerCount = playerCount;

    // move curtain upwards
    Vector3f pos = new Vector3f();
    initialPos = new Vector3f(getGameSettings().getInitialPos());
    initialPos.get(pos);
    Display pillarDisplay = getDisplay(pillar);
    move(pillarDisplay, pos.sub(pillarDisplay.getTransformation().getTranslation()),
        (int) (getGameSettings().getCurtainDelay() * 20)); // move pillar downwards

    // start rotating curtains
    scheduler.enqueue((long) (getGameSettings().getCurtainDelay() * 1000), () -> {
      SoundSet music = getGameSettings().getMusicSound();
//      rotatePlayer(true);

      interpolator.add(
          (long) ((music.getDuration() + getGameSettings().getMusicOffset() - 0.5) * 1000),
          (progress) -> {
            float angle = (float) (progress * Math.PI * 4);
            rotate(pillarDisplay, angle, 0);
          });

      playSoundForSpectators(music.getSound());
      this.soundPlayer.enqueue(getPlayers(), music.getSound(),
          music.getDuration() + getGameSettings().getMusicOffset(), () -> {
            Bukkit.getServer().stopSound(SoundStop.named(Key.key(music.getSound())));
            openCurtain();
          });
    });
  }

  public void teleportToCenter() {
    List<GamePlayer> players = new ArrayList<>(getPlayers(GameRole.TROY.getLevel()));
    float radius = getGameSettings().getLockDistance() -
        (random.nextInt(2) + 2);
    float angleRatio = 360f / players.size();
    float angle = 0;
    Vector3f center = getPillar().getLocation().toVector().toVector3f();
    for (GamePlayer player : players) {
      angle += angleRatio + 5;
      if (!player.available()) {
        continue;
      }

      float radian = (float) Math.toRadians(angle);
      Vector3f playerPos =
          new Vector3f((float) (radius * Math.cos(radian)), 0, (float) (radius * Math.sin(radian)));
      playerPos.add(center);
      Location loc = new Location(getPillar().getWorld(), playerPos.x, playerPos.y, playerPos.z,
          player.getPlayer().getYaw(), player.getPlayer().getPitch());
      player.getPlayer().teleport(loc);
    }
  }

  private void setupMounts(float rotationSpeed) {
    List<GamePlayer> players = new ArrayList<>(getPlayers(GameRole.TROY.getLevel()));
    Collections.shuffle(players);
    float radius = getGameSettings().getCurtainRadius() - getGameSettings().getCurtainOffset() -
        random.nextInt(2);

    float angleRatio = 360f / players.size();
    float angle = 0;
    Vector3f center = getPillar().getLocation().toVector().toVector3f();
    for (GamePlayer player : players) {
      angle += angleRatio;
      if (!player.available()) {
        continue;
      }

      float radian = (float) Math.toRadians(angle);
      Vector3f playerPos =
          new Vector3f((float) (radius * Math.cos(radian)), 0, (float) (radius * Math.sin(radian)));
      playerPos.add(center);
      playerPos.sub(0, 3, 0);
      Location loc = new Location(getPillar().getWorld(), playerPos.x, playerPos.y, playerPos.z);
      player.getPlayer().teleport(loc);

      Mount mount = new Mount(player, center, rotationSpeed);
      mountList.put(player.getId(), mount);
    }
  }

  private void rotate(Display display, float angle, float duration) {
    Transformation t = display.getTransformation();
    t.getLeftRotation().rotationXYZ(0, angle, 0);

    display.setInterpolationDuration((int) (duration * 20));
    display.setInterpolationDelay(-1);
    display.setTransformation(t);
  }

  public void openCurtain() {
//    rotatePlayer(false);
    gameStatus = MGRGameStatus.OPENING_CURTAIN;
    move(getDisplay(pillar), new Vector3f(0, getGameSettings().getCurtainMoveDistance(), 0),
        (int) (getGameSettings().getCurtainDelay() * 20));// move curtain entity upwards
    startJoiningRoom();
    scheduler.enqueue((long) ((getGameSettings().getCurtainDelay() / 2f) * 1000L), () -> {
      SoundSet set = getGameSettings().getAnnouncerSounds().get(playerCount);
      if (set == null) {
        log.error("No announcer sounds available for {} player counts.", playerCount);
        return;
      }
      playSoundForSpectators(set.getSound());
      this.soundPlayer.enqueue(getPlayers(), set.getSound(), getGameSettings().getJoiningRoomTime(),
          this::startClosingRoom);
    });

  }

  public void setDoorOpen(boolean flag) {
    Sound sound = flag ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
    for (Location loc : getGameSettings().getDoors().values()) {
      Block b = loc.getBlock();
      if (b.getBlockData() instanceof Openable door) {

        door.setOpen(flag);
        b.setBlockData(door);
        b.getState().update(true);
        b.getWorld().playSound(b.getLocation(), sound, 1.0f, 1.0f);
      }
    }
  }

  public void setDoorOpen(Block b, boolean flag) {
    if (b.getBlockData() instanceof Openable door) {
      Sound sound = flag ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE;
      door.setOpen(flag);
      b.setBlockData(door);
      b.getState().update(true);
      b.getWorld().playSound(b.getLocation(), sound, 1.0f, 1.0f);
    }
  }

  private void removeMounts() {
    for (Mount m : mountList.values()) {
      m.remove();
    }

    mountList.clear();
  }

  public void startJoiningRoom() {
    gameStatus = MGRGameStatus.JOINING_ROOM;
    joiningStartedTime = System.currentTimeMillis();

    setDoorOpen(true);
    setPumpkinHead(true);
    lockPlayer(false);
  }

  public void setPumpkinHead(boolean flag) {
    Collection<GamePlayer> players = getPlayers(GameRole.TROY.getLevel());

    if (!flag) {
      for (GamePlayer player : players) {
        if (!player.available()) {
          continue;
        }
        player.getPlayer().getInventory()
            .setItem(EquipmentSlot.HEAD, new ItemStack(Material.AIR));
      }
    } else {
      for (GamePlayer player : players) {
        if (!player.available()) {
          continue;
        }
        player.getPlayer().getInventory()
            .setItem(EquipmentSlot.HEAD, new ItemStack(Material.CARVED_PUMPKIN));
      }
    }

  }

  public void startClosingRoom() {
    gameStatus = MGRGameStatus.CLOSING_ROOM;
    setDoorOpen(false);
    List<Component> success = new ArrayList<>();
    List<Component> failed = new ArrayList<>();

    Collection<Player> adminPlayers = getOnlinePlayers(GameRole.ADMIN);
    for (Map.Entry<Integer, Region> entry : ((MGRGameSettings) getSettings()).getRooms()
        .entrySet()) {
      int roomNum = entry.getKey();
      Region roomRegion = entry.getValue();
      Collection<Entity> playersInRoom = getRoomPlayers(roomRegion);
      int count = playersInRoom.size();
      if (playerCount != count) {
        PlayerUtils.enableEntityGlowing(playersInRoom, adminPlayers, glowingColor);
        Component comp = Component.text(roomNum + "번").append(Component.text("(" + count + "명)"));
        if (count > 0) {
          comp = comp.style(Style.style(NamedTextColor.AQUA));
        }
        failed.add(comp);
      } else {
        Component comp = Component.text(roomNum + "번").append(Component.text("(" + count + "명)"));
        if (count > 0) {
          comp = comp.style(Style.style(NamedTextColor.AQUA));
        }
        success.add(comp);
      }
    }
    broadcast(
        Component.text("인원 충족: [").append(Component.join(JoinConfiguration.commas(false), success))
            .append(Component.text("]")).style(Style.style(NamedTextColor.GREEN)), GameRole.ADMIN);
    broadcast(
        Component.text("인원 미충족: [").append(Component.join(JoinConfiguration.commas(false), failed))
            .append(Component.text("]")).style(Style.style(NamedTextColor.YELLOW)), GameRole.ADMIN);

  }

  public void closeSession() {
    move(getDisplay(pillar), getGameSettings().getPillarLocation().toVector().toVector3f()
            .sub(pillar.getLocation().toVector().toVector3f()),
        (int) (getGameSettings().getCurtainDelay() * 20));
    scheduler.enqueue((long) (getGameSettings().getCurtainDelay() * 1000), () -> {
      gameStatus = MGRGameStatus.IDLE;
    });
  }

  public void setBarrier() {
    if (barrierCache.isEmpty()) {
      barrierCache = createBarrier(getPillar().getLocation(),
          (int) getGameSettings().getCurtainMoveDistance(), getGameSettings().getCurtainRadius());
    }
    for (Location l : barrierCache) {
      l.getBlock().setType(Material.BARRIER);
    }
  }

  public void removeBarrier() {
    if (barrierCache.isEmpty()) {
      barrierCache = createBarrier(getPillar().getLocation(),
          (int) getGameSettings().getCurtainMoveDistance(), getGameSettings().getCurtainRadius());
    }
    for (Location l : barrierCache) {
      l.getBlock().setType(Material.AIR);
    }
  }

  private Set<Location> createBarrier(Location center, int height, float radius) {
    Set<Location> lower = new HashSet<>();
    for (int i = 0; i < 360; i++) {
      float angle = (float) Math.toRadians(i);
      float x = center.getBlockX() + (float) Math.cos(angle) * radius;
      float y = center.getBlockY();
      float z = center.getBlockZ() + (float) Math.sin(angle) * radius;

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

  private Collection<Entity> getRoomPlayers(Region region) {
    World world = region.getStart().getWorld();
    BoundingBox range =
        new BoundingBox(region.getStart().getBlockX(), region.getStart().getBlockY(),
            region.getStart().getBlockZ(),
            region.getEnd().getBlockX(), region.getEnd().getBlockY(), region.getEnd().getBlockZ());
    return world.getNearbyEntities(range, entity -> {
      if (!(entity instanceof Player player)) {
        return false;
      }
      GamePlayer gamePlayer = getPlayer(player.getUniqueId());
      if (gamePlayer == null) {
        return false;
      }
      return gamePlayer.getRole().getLevel() <= GameRole.TROY.getLevel();
    });
  }
}
