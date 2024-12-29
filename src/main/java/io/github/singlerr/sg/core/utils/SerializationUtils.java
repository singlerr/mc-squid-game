package io.github.singlerr.sg.core.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.experimental.UtilityClass;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import org.bukkit.craftbukkit.v1_20_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.entity.ArmorStand;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@UtilityClass
public class SerializationUtils {

  public void writeVector3f(Vector3f vec, ByteArrayDataOutput buffer) {
    buffer.writeFloat(vec.x);
    buffer.writeFloat(vec.y);
    buffer.writeFloat(vec.z);
  }

  public void writeQuaternion(Quaternionf quat, ByteArrayDataOutput buffer) {
    buffer.writeFloat(quat.x);
    buffer.writeFloat(quat.y);
    buffer.writeFloat(quat.z);
    buffer.writeFloat(quat.w);
  }

  public Vector3f readVector3f(ByteArrayDataInput buffer) {
    float x = buffer.readFloat();
    float y = buffer.readFloat();
    float z = buffer.readFloat();
    return new Vector3f(x, y, z);
  }

  public Quaternionf readQuaternion(ByteArrayDataInput buffer) {
    float x = buffer.readFloat();
    float y = buffer.readFloat();
    float z = buffer.readFloat();
    float w = buffer.readFloat();
    return new Quaternionf(x, y, z, w);
  }

  public void writeTransformationData(Transform transform, ArmorStand armorStand) {
    CraftPersistentDataContainer tag =
        (CraftPersistentDataContainer) armorStand.getPersistentDataContainer();
    CompoundTag transformTag = new CompoundTag();
    if (transform.getTranslation() != null) {
      CompoundTag t = new CompoundTag();
      writeVector3f(transform.getTranslation(), t);
      transformTag.put("translation", t);
    }
    if (transform.getRotation() != null) {
      CompoundTag r = new CompoundTag();
      writeQuaternionf(transform.getRotation(), r);
      transformTag.put("rotation", r);
    }
    if (transform.getScale() != null) {
      CompoundTag s = new CompoundTag();
      writeVector3f(transform.getScale(), s);
      transformTag.put("scale", s);
    }

    tag.put("ModelTransformation", transformTag);
  }

  public void writeAnimationData(Animation animation, ArmorStand armorStand) {
    CraftPersistentDataContainer tag =
        (CraftPersistentDataContainer) armorStand.getPersistentDataContainer();
    CompoundTag animationTag = new CompoundTag();
    if (animation.getTransform().getTranslation() != null) {
      CompoundTag t = new CompoundTag();
      writeVector3f(animation.getTransform().getTranslation(), t);
      animationTag.put("translation", t);
    }
    if (animation.getTransform().getRotation() != null) {
      CompoundTag r = new CompoundTag();
      writeQuaternionf(animation.getTransform().getRotation(), r);
      animationTag.put("rotation", r);
    }
    if (animation.getTransform().getScale() != null) {
      CompoundTag s = new CompoundTag();
      writeVector3f(animation.getTransform().getScale(), s);
      animationTag.put("scale", s);
    }

    animationTag.putInt("nodeIndex", animation.getNodeIndex());
    tag.put("ModelAnimation", animationTag);
  }

  public void writeVector3f(Vector3f vec, CompoundTag tag) {
    tag.putFloat("x", vec.x);
    tag.putFloat("y", vec.y);
    tag.putFloat("z", vec.z);
  }

  public void writeQuaternionf(Quaternionf quat, CompoundTag tag) {
    tag.putFloat("x", quat.x);
    tag.putFloat("y", quat.y);
    tag.putFloat("z", quat.z);
    tag.putFloat("w", quat.w);
  }

  public void writeModelData(String modelLocation, ArmorStand armorStand) {
    CraftPersistentDataContainer tag =
        (CraftPersistentDataContainer) armorStand.getPersistentDataContainer();
    tag.put("ModelLocation", StringTag.valueOf(modelLocation));
  }
}
