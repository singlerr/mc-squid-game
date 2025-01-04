package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.Region;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Vector3f;

@Slf4j
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
      for (Entity passenger : entity.getPassengers()) {
        if (passenger instanceof Display display) {
          Transformation t = display.getTransformation();
          display.setInterpolationDelay(0);
          display.setInterpolationDuration((int) ((duration / 1000) * 20));
          t.getTranslation().add(new Vector3f((float) direction.getX(), (float) direction.getY(),
              (float) direction.getZ()));
          display.setTransformation(t);
        }
      }
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
    Location loc = startPoint.clone().add(direction.clone().setY(0).multiply(p));
    entity.teleport(loc);
  }

  public boolean end() {
    return end;
  }
}
