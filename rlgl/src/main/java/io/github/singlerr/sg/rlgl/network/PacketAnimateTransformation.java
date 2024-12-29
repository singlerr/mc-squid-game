package io.github.singlerr.sg.rlgl.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import io.github.singlerr.sg.core.network.Packet;
import io.github.singlerr.sg.core.utils.SerializationUtils;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class PacketAnimateTransformation implements Packet {

  public static final String ID = "vanilla-gltf:animate_transformation";

  private Vector3f translation;
  private Quaternionf rotation;
  private Vector3f scale;

  private long duration;

  @Override
  public void writePayload(ByteArrayDataOutput buffer) {
    SerializationUtils.writeVector3f(translation, buffer);
    SerializationUtils.writeQuaternion(rotation, buffer);
    SerializationUtils.writeVector3f(scale, buffer);
    buffer.writeLong(duration);
  }

  @Override
  public void readPayload(ByteArrayDataInput buffer) {
    translation = SerializationUtils.readVector3f(buffer);
    rotation = SerializationUtils.readQuaternion(buffer);
    scale = SerializationUtils.readVector3f(buffer);
    duration = buffer.readLong();
  }

  @Override
  public void handle(Player player) {
    // no op
  }
}
