package io.github.singlerr.admin;

import io.github.singlerr.sg.core.utils.Transform;
import java.util.UUID;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

@Data
public class EntityReference {

  private UUID id;
  private String modelLocation;
  private String world;
  private Transform transform;

  private transient Entity entity;

  public EntityReference(UUID id, String modelLocation, String world, Transform transform) {
    this.id = id;
    this.modelLocation = modelLocation;
    this.world = world;
    this.transform = transform;
    sync();
  }

  public EntityReference(UUID id, String modelLocation, String world, Entity entity,
                         Transform transform) {
    this.id = id;
    this.modelLocation = modelLocation;
    this.world = world;
    this.entity = entity;
    this.transform = transform;
  }

  public static EntityReference of(UUID id, String modelLocation, Transform transform,
                                   Entity entity) {
    return new EntityReference(id, modelLocation, entity.getWorld().getName(), entity, transform);
  }

  public static EntityReference of(UUID id, String modelLocation, String world,
                                   Transform transform) {
    return new EntityReference(id, modelLocation, world, transform);
  }

  public boolean sync() {
    World w = Bukkit.getWorld(world);
    if (w == null) {
      return false;
    }
    if (entity == null) {
      entity = w.getEntity(id);
    }
    return entity != null;
  }
}
