package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Animation;
import io.github.singlerr.sg.core.utils.ArmorStandUtils;
import io.github.singlerr.sg.core.utils.Interpolator;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import io.github.singlerr.sg.core.utils.Transform;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.ArmorStand;
import org.joml.Quaternionf;

@Slf4j
public final class RLGLGameContext extends GameContext {

  @Getter
  private final Set<UUID> killTargets;
  @Getter
  private final TickableSoundPlayer soundPlayer;
  @Getter
  @Setter
  private long startTime;
  @Getter
  @Setter
  private RLGLStatus rlglStatus;

  @Setter
  @Getter
  private ArmorStand youngHee;

  @Getter
  @Setter
  private Interpolator rotationAnimator;

  public RLGLGameContext(List<GamePlayer> players,
                         GameStatus status,
                         GameEventBus eventBus,
                         GameSettings settings) {
    super(players, status, eventBus, settings);
    this.killTargets = new HashSet<>();
    this.soundPlayer = new TickableSoundPlayer();
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
  }

  public void end() {
    rlglStatus = RLGLStatus.IDLE;
  }

  public void redLight() {
    rlglStatus = RLGLStatus.RED_LIGHT;
    setRotationAnimator(
        new Interpolator((long) (1000 * getGameSettings().getGreenLightDelay()),
            p -> rotateBackward(youngHee, p)));
  }

  public void greenLight(SoundSet set) {
    rlglStatus = RLGLStatus.GREEN_LIGHT;
    setRotationAnimator(new Interpolator((long) (1000 * set.getDuration()),
        p -> rotateForward(youngHee, p)));
    this.soundPlayer.enqueue(getPlayers(), set.getSound(), set.getDuration(),
        this::redLight);
  }

  private void rotateForward(ArmorStand armorStand, float progress) {
    Quaternionf rot = new Quaternionf();
    rot.rotationXYZ(0, (float) (Math.PI * progress), 0);
    try {
      ArmorStandUtils.animate(armorStand,
          new Animation(getGameSettings().getNodeIndex(), new Transform(null, rot, null)));
    } catch (Throwable t) {
      log.error("Failed to animate armorstand", t);
    }
  }

  private void rotateBackward(ArmorStand armorStand, float progress) {
    Quaternionf rot = new Quaternionf();
    rot.rotationXYZ(0, (float) (Math.PI * (1 + progress)), 0);
    try {
      ArmorStandUtils.animate(armorStand,
          new Animation(getGameSettings().getNodeIndex(), new Transform(null, rot, null)));
    } catch (Throwable t) {
      log.error("Failed to animate armorstand", t);
    }
  }


}
