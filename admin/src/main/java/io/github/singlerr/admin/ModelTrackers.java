package io.github.singlerr.admin;

import io.github.singlerr.sg.core.utils.Transform;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.experimental.UtilityClass;
import org.bukkit.entity.Entity;

@UtilityClass
public class ModelTrackers {
  private Map<UUID, EntityReference> loadedEntities = Collections.synchronizedMap(new HashMap<>());

  public void addEntity(UUID id, String modelLocation, Entity entity, Transform transform) {
    loadedEntities.put(id, EntityReference.of(id, modelLocation, transform, entity));
  }

  public void addEntity(UUID id, EntityReference reference) {
    loadedEntities.put(id, reference);
  }

  public void remove(UUID id) {
    loadedEntities.remove(id);
  }

  public Collection<EntityReference> entitiesNotNull() {
    return loadedEntities.values().stream().filter(EntityReference::sync).toList();
  }

  public Map<UUID, EntityReference> entriesNotNull() {
    Map<UUID, EntityReference> map = new HashMap<>();
    for (Map.Entry<UUID, EntityReference> e : loadedEntities.entrySet()) {
      if (e.getValue().sync()) {
        map.put(e.getKey(), e.getValue());
      }
    }

    return map;
  }

}
