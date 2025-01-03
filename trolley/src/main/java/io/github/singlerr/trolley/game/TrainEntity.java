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
    this.direction = this.endPoint.toVector().subtract(this.startPoint.toVector());
    this.duration = duration;
    this.radius = radius;
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

    float p = (float) timePassed / (float) duration;

    int d = (int) endPoint.distance(entity.getLocation());
    if (d <= 0) {
      end = true;
      return;
    }

    List<Entity> players = entity.getNearbyEntities(radius, radius, radius).stream().filter(
            e -> context.getPlayer(e.getUniqueId()) != null &&
                context.getPlayer(e.getUniqueId()).getRole().getLevel() <= GameRole.TROY.getLevel())
        .toList();
    players.forEach(e -> ((Player) e).setHealth(0));

    entity.teleport(startPoint.clone().add(direction.clone().setY(0).multiply(p)));
  }

  public boolean end() {
    return end;
  }
}
