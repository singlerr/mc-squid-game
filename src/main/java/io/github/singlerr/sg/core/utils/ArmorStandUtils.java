package io.github.singlerr.sg.core.utils;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import lombok.experimental.UtilityClass;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.ArmorStand;

@UtilityClass
public class ArmorStandUtils {

  private final MethodHandle UPDATE_ARMORSTAND;

  static {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodType updateMethodType = MethodType.methodType(CraftEntity.class);
    try {
      UPDATE_ARMORSTAND = lookup.findVirtual(CraftEntity.class, "update", updateMethodType);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  public void transform(ArmorStand armorStand, Transform transform) throws Throwable {
    SerializationUtils.writeTransformationData(transform, armorStand);
    CraftEntity handle = (CraftEntity) armorStand;
    UPDATE_ARMORSTAND.invoke(handle);
  }

  public void animate(ArmorStand armorStand, Animation animation) throws Throwable {
    SerializationUtils.writeAnimationData(animation, armorStand);
    CraftEntity handle = (CraftEntity) armorStand;
    UPDATE_ARMORSTAND.invoke(handle);
  }
}
