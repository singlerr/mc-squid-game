package io.github.singlerr.trolley.game;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.Region;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.Bukkit;
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
  private final Train train;
  private final long duration;
  private final float radius;
  private boolean end;

  private boolean started;
  private long startTime;

  private Vector3f originalTranslation;
  private Display display;

  public TrainEntity(TrolleyGameContext context, Train train, Entity entity, long duration,
                     float radius,
                     Region railway) {
    this.context = context;
    this.entity = entity;
    this.train = train;
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
    if (entity.getPassengers().isEmpty()) {
      display = train.getEntity().getPassengers().stream().map(EntitySerializable::toEntity)
          .filter(e -> e instanceof Display).map(e -> (Display) e).findAny().orElse(null);
      if (display != null) {
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(0);
        display.setTeleportDuration(0);
        Transformation t = display.getTransformation();
        originalTranslation = t.getTranslation();
        display.teleport(startPoint);
        entity.addPassenger(display);
      }
    } else {
      display =
          entity.getPassengers().stream().filter(e -> e instanceof Display).map(e -> (Display) e)
              .findAny().orElse(null);
      if (display != null) {
        display.setInterpolationDelay(0);
        display.setInterpolationDuration(0);
        display.setTeleportDuration(0);
        Transformation t = display.getTransformation();
        display.teleport(startPoint);
        originalTranslation = t.getTranslation();
      }
    }

  }

  public void tick(long time) {
    if (end()) {
      return;
    }

    if (!started) {
      startTime = time;
      started = true;
      Transformation t = display.getTransformation();
      t.getTranslation().set(direction.toVector3f().mul(1, 0, 1));
      display.setInterpolationDuration((int) duration / 1000 * 20);
      display.setTransformation(t);
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
    Location loc = startPoint.clone().add(direction.clone().setY(0).multiply(p));

    List<Entity> players =
        entity.getWorld().getNearbyEntities(loc, radius, radius + 2, radius).stream().filter(
                e -> context.getPlayer(e.getUniqueId()) != null &&
                    context.getPlayer(e.getUniqueId()).getRole().getLevel() <= GameRole.TROY.getLevel())
            .toList();
    players.forEach(e -> killPlayer((Player) e));

  }

  private void killPlayer(Player player) {
    Vector dir = new Vector(direction.getX(), direction.getY(), direction.getZ()).normalize();
    player.setVelocity(dir);
    Bukkit.getScheduler().scheduleSyncDelayedTask(GameCore.getInstance(), () -> {
      player.setHealth(0);
    }, 5L);
  }


  public void reset() {
    Display display = train.getEntity().getPassengers().stream().map(EntitySerializable::toEntity)
        .filter(e -> e instanceof Display).map(e -> (Display) e).findAny().orElse(null);
    if (display != null) {
      display.setInterpolationDuration(0);
      Transformation t = display.getTransformation();
      t.getTranslation().set(originalTranslation);
      display.setTransformation(t);
    }
    entity.teleport(startPoint);
  }

  public boolean end() {
    return end;
  }
}
