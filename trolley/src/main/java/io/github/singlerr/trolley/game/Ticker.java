package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.utils.Region;
import io.github.singlerr.trolley.network.PacketIntermissionResult;
import java.util.Deque;
import java.util.function.Consumer;
import lombok.Data;

public final class Ticker {

  private final StatusSwitch SLOWED;
  private final StatusSwitch INTERMISSION;
  private final StatusSwitch MISSION_FAILED;
  private final StatusSwitch MISSION_SUCCEEDED;

  private final TrolleyGameContext context;
  private final Region region;
  private final GamePlayer player;
  private final long slowedTime;
  private final long intermissionLimitTime;
  private final long idleTime;
  private final float idleSpeed;
  private final float slowedSpeed;
  private StatusSwitch current;
  private Deque<StatusSwitch> nextStatus;

  public Ticker(TrolleyGameContext context, Region region, GamePlayer player, float idleSpeed,
                float slowedSpeed, long slowedTime, long intermissionLimitTime, long idleTime) {
    this.context = context;
    this.region = region;
    this.player = player;
    this.slowedTime = slowedTime;
    this.intermissionLimitTime = intermissionLimitTime;
    this.idleTime = idleTime;

    this.slowedSpeed = slowedSpeed;
    this.idleSpeed = idleSpeed;

    this.SLOWED =
        new StatusSwitch(PlayerTrolleyStatus.INTERMISSION, this::requestIntermission, slowedTime);
    this.INTERMISSION = new StatusSwitch(PlayerTrolleyStatus.SLOWED, this::intermissionFailed,
        intermissionLimitTime);
    this.MISSION_FAILED = new StatusSwitch(PlayerTrolleyStatus.SLOWED, this::slowed, idleTime);
    this.MISSION_SUCCEEDED = new StatusSwitch(PlayerTrolleyStatus.IDLE, this::slowed, idleTime);
  }

  private boolean shouldTick() {
    return region.isIn(player.getPlayer().getLocation());
  }

  public void tick(long time) {
    if (!player.available()) {
      return;
    }

    if (!shouldTick()) {
      player.getPlayer().setWalkSpeed(idleSpeed);
      return;
    }
    if (current == null) {
      if (!nextStatus.isEmpty()) {
        current = nextStatus.removeFirst();
        current.setStartTime(time);
      }
      return;
    }
    if (current.end(time)) {
      current.getTask().accept(current);
      if (!nextStatus.isEmpty()) {
        current = nextStatus.removeFirst();
        current.setStartTime(time);
      }
    }
  }

  public void start() {
    current = null;
    nextStatus.clear();
    nextStatus.add(SLOWED.clone());
  }

  private void slowed(StatusSwitch self) {
    nextStatus.add(SLOWED.clone());
  }

  private void intermissionFailed(StatusSwitch self) {
    if (!self.isIntermission()) {
      return;
    }
    if (!player.available()) {
      return;
    }

    player.getPlayer().setWalkSpeed(self.isIntermissionSuccess() ? idleSpeed : slowedSpeed);
  }

  public void handleIntermissionResult(PacketIntermissionResult result) {
    if (!current.isIntermission()) {
      return;
    }

    current.setEnd(true);
    current.setIntermissionSuccess(result.isSuccess());
    StatusSwitch next = result.isSuccess() ? MISSION_SUCCEEDED.clone() : MISSION_FAILED.clone();
    nextStatus.add(next);
  }

  private void requestIntermission(StatusSwitch self) {
    // send packet to request
    if (!player.available()) {
      return;
    }
    context.sendIntermissionRequest(player.getPlayer());
    nextStatus.add(INTERMISSION.clone());
  }


  /***
   * game start ->
   * slowed time -> intermission -> slowed (if mission failed) / idle (if mission succeeded) -> slowed -> intermission
   */
  // task is called when delay is done
  @Data
  private static class StatusSwitch implements Cloneable {

    private final PlayerTrolleyStatus nextStatus;
    private final Consumer<StatusSwitch> task;
    private final long delay;
    private long startTime;

    private boolean intermission = false;
    private boolean intermissionSuccess = false;
    // end for killing this job earlier than expected
    private boolean end = false;

    public StatusSwitch(PlayerTrolleyStatus nextStatus, Consumer<StatusSwitch> task, long delay) {
      this.nextStatus = nextStatus;
      this.task = task;
      this.delay = delay;
    }

    public StatusSwitch(PlayerTrolleyStatus nextStatus, Consumer<StatusSwitch> task, long delay,
                        boolean intermission) {
      this.nextStatus = nextStatus;
      this.task = task;
      this.delay = delay;
      this.intermission = intermission;
    }

    public boolean end(long time) {
      return end || time - startTime >= delay;
    }

    @Override
    public StatusSwitch clone() {
      return new StatusSwitch(nextStatus, task, delay);
    }
  }
}
