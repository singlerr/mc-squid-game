package io.github.singlerr.sg.core.utils;

import lombok.experimental.UtilityClass;
import net.minecraft.network.FriendlyByteBuf;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@UtilityClass
public class PacketUtils {

  public Transform readTransform(FriendlyByteBuf buf) {
    boolean t = buf.readBoolean();
    boolean r = buf.readBoolean();
    boolean s = buf.readBoolean();
    Vector3f tr = null;
    Quaternionf rot = null;
    Vector3f sc = null;
    if (t) {
      tr = buf.readVector3f();
    }
    if (r) {
      rot = buf.readQuaternion();
    }
    if (s) {
      sc = buf.readVector3f();
    }

    return new Transform(tr, rot, sc);
  }

  public void writeTransform(Transform transform, FriendlyByteBuf buf) {
    boolean t = transform.getTranslation() != null;
    boolean r = transform.getRotation() != null;
    boolean s = transform.getScale() != null;
    buf.writeBoolean(t);
    buf.writeBoolean(r);
    buf.writeBoolean(s);
    if (t) {
      buf.writeVector3f(transform.getTranslation());
    }
    if (r) {
      buf.writeQuaternion(transform.getRotation());
    }
    if (s) {
      buf.writeVector3f(transform.getScale());
    }
  }
}
