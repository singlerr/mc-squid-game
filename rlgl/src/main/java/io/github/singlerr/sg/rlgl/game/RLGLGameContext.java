package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.TickableSoundPlayer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

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

  public RLGLGameContext(List<GamePlayer> players,
                         GameStatus status,
                         GameEventBus eventBus,
                         GameSettings settings) {
    super(players, status, eventBus, settings);
    this.killTargets = new HashSet<>();
    this.soundPlayer = new TickableSoundPlayer();
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
    RLGLGameSettings settings = (RLGLGameSettings) getSettings();
    this.soundPlayer.enqueue(getPlayers(), settings.getRedLightSound(), settings.getRedLightDelay(),
        () -> {
          rlglStatus = RLGLStatus.RED_LIGHT;
        });
  }

  public void greenLight() {
    rlglStatus = RLGLStatus.GREEN_LIGHT;
  }
}
