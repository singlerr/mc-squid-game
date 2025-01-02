package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.Region;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public final class TrainEntity {

  private final TrolleyGameContext context;
  private final Location startPoint;
  private final Location endPoint;
  private final Vector direction;
  private final Entity entity;
  private final float coefficient;
  private final long duration;
  private final float radius;
  private boolean end;

  private boolean started;
  private long startTime;

  public TrainEntity(TrolleyGameContext context, Entity entity, long duration, float radius,
                     Region railway) {
    this.context = context;
    this.entity = entity;
    this.startPoint = railway.getStart().clone();
    this.endPoint = railway.getEnd().clone();
    this.direction = this.startPoint.toVector().subtract(this.endPoint.toVector()).normalize();
    this.duration = duration;
    this.radius = radius;
    double distance = this.startPoint.distance(this.endPoint);
    this.coefficient = (float) distance / (float) (duration / 1000L);
    this.end = false;
    this.started = false;
  }

  public void init() {
    entity.teleport(startPoint);
  }

  public void tick(long time) {
    if (end()) {
      return;
    }

    if (!started) {
      startTime = time;
      started = true;
    }

    long timePassed = time - startTime;
    if (timePassed >= duration) {
      end = true;
      return;
    }

    double d = endPoint.distance(entity.getLocation());
    if (d <= coefficient) {
      end = true;
      return;
    }

    List<Entity> players = entity.getNearbyEntities(radius, radius, radius).stream().filter(
            e -> context.getPlayer(e.getUniqueId()) != null &&
                context.getPlayer(e.getUniqueId()).getRole().getLevel() <= GameRole.TROY.getLevel())
        .toList();
    players.forEach(e -> ((Player) e).setHealth(0));

    entity.setVelocity(direction.multiply(coefficient));
  }

  public boolean end() {
    return end;
  }
}
