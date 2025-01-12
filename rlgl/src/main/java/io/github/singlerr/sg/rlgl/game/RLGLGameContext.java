package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Interpolator;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.TaskScheduler;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Mth;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;

@Slf4j
public final class RLGLGameContext extends GameContext {

  @Getter
  private final Set<UUID> killTargets;
  @Getter
  private final TickableSoundPlayer soundPlayer;
  @Getter
  private final NetworkRegistry network;
  @Getter
  private final ChatColor glowingColor;
  @Getter
  @Setter
  private long startTime;
  @Getter
  @Setter
  private RLGLStatus rlglStatus;
  @Setter
  @Getter
  private Entity youngHee;
  @Getter
  @Setter
  private Interpolator rotationAnimator;
  @Getter
  @Setter
  private TaskScheduler scheduler;

  public RLGLGameContext(Map<UUID, GamePlayer> players,
                         GameStatus status,
                         GameEventBus eventBus,
                         GameSettings settings) {
    super(players, status, eventBus, settings);
    this.killTargets = new HashSet<>();
    this.soundPlayer = new TickableSoundPlayer();
    this.scheduler = new TaskScheduler();
    this.network = Bukkit.getServer().getServicesManager().getRegistration(NetworkRegistry.class)
        .getProvider();
    this.setRlglStatus(RLGLStatus.IDLE);
    this.glowingColor = ChatColor.AQUA;
  }

  public RLGLGameSettings getGameSettings() {
    return (RLGLGameSettings) getSettings();
  }

  public void start() {
    RLGLGameSettings settings = (RLGLGameSettings) getSettings();
    this.soundPlayer.enqueue(getPlayers(), settings.getStartSound(), settings.getStartDelay(),
        () -> {
          startTime = System.currentTimeMillis();
          rlglStatus = RLGLStatus.GREEN_LIGHT;
        });
    for (GamePlayer player : getPlayers(GameRole.TROY.getLevel())) {
      if (!player.available()) {
        continue;
      }
      player.getPlayer().setWalkSpeed(getGameSettings().getPlayerSpeed());
    }
  }

  public void end() {
    rlglStatus = RLGLStatus.IDLE;
  }

  public void redLight() {
    killTargets.clear();
    if (youngHee instanceof Display display) {
      Transformation t = display.getTransformation();
      Quaternionf rot = t.getLeftRotation();
      rot.rotateZ(Mth.PI);
      display.setInterpolationDelay(0);
      display.setInterpolationDuration((int) (getGameSettings().getRedLightTurnDelay() * 20));
      display.setTransformation(t);
    }
    soundPlayer.enqueue(getPlayers(), getGameSettings().getTransitionSound().getSound(),
        getGameSettings().getRedLightTurnDelay(), () -> {
          rlglStatus = RLGLStatus.RED_LIGHT;
        });
  }

  public void greenLight(SoundSet set) {
    PlayerUtils.disableGlowing(getOnlinePlayers(GameRole.TROY.getLevel()),
        getOnlinePlayers(GameRole.ADMIN));

    rlglStatus = RLGLStatus.GREEN_LIGHT;
    if (youngHee instanceof Display display) {
      Transformation t = display.getTransformation();
      Quaternionf rot = t.getLeftRotation();
      if (!rot.equals(getGameSettings().getOriginalRot().x, getGameSettings().getOriginalRot().y,
          getGameSettings().getOriginalRot().z, getGameSettings().getOriginalRot().w)) {
        rot.rotateZ(-Mth.PI);
        display.setInterpolationDelay(0);
        display.setInterpolationDuration((int) (getGameSettings().getRedLightTurnDelay() * 20));
        display.setTransformation(t);
      }
    }
    soundPlayer.enqueue(getPlayers(), getGameSettings().getTransitionSound().getSound(),
        getGameSettings().getGreenLightTurnDelay(), () -> {
          this.soundPlayer.enqueue(getPlayers(), set.getSound(), set.getDuration(),
              this::redLight);
        });
  }

}
