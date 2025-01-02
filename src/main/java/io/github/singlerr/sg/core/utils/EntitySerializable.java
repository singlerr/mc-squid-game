package io.github.singlerr.sg.core.utils;

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


  public static EntitySerializable of(Entity entity) {
    return new EntitySerializable(entity.getWorld().getName(), entity.getUniqueId());
  }
}
