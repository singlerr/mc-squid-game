package io.github.singlerr.admin;

import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.Data;
import org.bukkit.entity.Entity;

@Data
public class EntityReference {

  private String modelLocation;
  private EntitySerializable serializedEntity;

  private transient Entity entity;

  public EntityReference(String modelLocation, EntitySerializable serializedEntity) {
    this.modelLocation = modelLocation;
    this.serializedEntity = serializedEntity;
    sync();
  }

  public static EntityReference of(String modelLocation, EntitySerializable serializedEntity) {
    return new EntityReference(modelLocation, serializedEntity);
  }

  public static EntityReference of(String modelLocation, Entity entity) {
    return new EntityReference(modelLocation, EntitySerializable.of(entity));
  }

  public boolean sync() {
    if (entity == null) {
      entity = serializedEntity.toEntity();
    }
    return entity != null;
  }
}
