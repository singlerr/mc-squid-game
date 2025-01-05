package io.github.singlerr.sg.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class EntitySerializable {

  private String world;
  private UUID id;
  private List<EntitySerializable> passengers = new ArrayList<>();

  public static EntitySerializable of(Entity entity) {
    return new EntitySerializable(entity.getWorld().getName(), entity.getUniqueId(),
        entity.getPassengers().isEmpty() ? new ArrayList<>() : entity.getPassengers().stream().map(
            EntitySerializable::of).toList());
  }

  public Entity toEntity() {
    World w = Bukkit.getWorld(world);
    if (w == null) {
      return null;
    }
    Entity e = w.getEntity(id);
    if (e == null) {
      return null;
    }
    for (EntitySerializable passenger : passengers) {
      Entity p = w.getEntity(passenger.getId());
      if (p != null) {
        e.addPassenger(p);
      }
    }
    return e;
  }

}
