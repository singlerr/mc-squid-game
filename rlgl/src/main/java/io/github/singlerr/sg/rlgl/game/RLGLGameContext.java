package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketAnimateTransformationModel;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.Animation;
import io.github.singlerr.sg.core.utils.Interpolator;
import io.github.singlerr.sg.core.utils.SoundSet;
import io.github.singlerr.sg.core.utils.TaskScheduler;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;

@Slf4j
public final class RLGLGameContext extends GameContext {

  @Getter
  private final Set<UUID> killTargets;
  @Getter
  private final TickableSoundPlayer soundPlayer;
  @Getter
  private final NetworkRegistry network;
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
  @Getter
  @Setter
  private TaskScheduler scheduler;

  public RLGLGameContext(List<GamePlayer> players,
                         GameStatus status,
                         GameEventBus eventBus,
                         GameSettings settings) {
    super(players, status, eventBus, settings);
    this.killTargets = new HashSet<>();
    this.soundPlayer = new TickableSoundPlayer();
    this.scheduler = new TaskScheduler();
    this.network = Bukkit.getServer().getServicesManager().getRegistration(NetworkRegistry.class)
        .getProvider();
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
    PacketAnimateTransformationModel pkt =
        new PacketAnimateTransformationModel(youngHee.getUniqueId(), youngHee.getEntityId(),
            new Animation(getGameSettings().getNodeIndex(), getGameSettings().getBackState(),
                getGameSettings().getFrontState(),
                (long) (getGameSettings().getRedLightTurnDelay() * 1000)));
    network.getChannel().sendToAll(pkt);
  }

  public void greenLight(SoundSet set) {
    PacketAnimateTransformationModel pkt =
        new PacketAnimateTransformationModel(youngHee.getUniqueId(), youngHee.getEntityId(),
            new Animation(getGameSettings().getNodeIndex(), getGameSettings().getFrontState(),
                getGameSettings().getBackState(),
                (long) (getGameSettings().getGreenLightTurnDelay() * 1000)));
    network.getChannel().sendToAll(pkt);
    scheduler.enqueue((long) (getGameSettings().getGreenLightTurnDelay() * 1000), () -> {
      rlglStatus = RLGLStatus.GREEN_LIGHT;
      this.soundPlayer.enqueue(getPlayers(), set.getSound(), set.getDuration(),
          this::redLight);
    });
  }

}
