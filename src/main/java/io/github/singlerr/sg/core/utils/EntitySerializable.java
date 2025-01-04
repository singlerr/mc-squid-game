package io.github.singlerr.sg.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
}
