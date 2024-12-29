package io.github.singlerr.sg.core.utils;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import lombok.experimental.UtilityClass;
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
}
