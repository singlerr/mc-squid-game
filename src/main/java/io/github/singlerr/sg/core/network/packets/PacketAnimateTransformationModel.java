package io.github.singlerr.sg.core.network.packets;

import io.github.singlerr.sg.core.network.Packet;
import io.github.singlerr.sg.core.utils.Animation;
import io.github.singlerr.sg.core.utils.PacketUtils;
import io.github.singlerr.sg.core.utils.Transform;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

@Getter
@AllArgsConstructor
public final class PacketAnimateTransformationModel implements Packet {

  public static final String ID = "vanilla-gltf:animate_transformation";

  private UUID id;
  private int entityId;
  private Animation animation;

  public PacketAnimateTransformationModel() {
  }

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeUUID(id);
    buffer.writeInt(entityId);
    writeAnimation(animation, buffer);
  }

  @Override
  public void readPayload(FriendlyByteBuf buf) {
    id = buf.readUUID();
    entityId = buf.readInt();
    animation = readAnimation(buf);
  }

  private Animation readAnimation(FriendlyByteBuf buffer) {
    int nodeIndex = buffer.readInt();
    long duration = buffer.readLong();
    Transform from = PacketUtils.readTransform(buffer);
    Transform to = PacketUtils.readTransform(buffer);
    return new Animation(nodeIndex, from, to, duration);
  }

  private void writeAnimation(Animation animation, FriendlyByteBuf buffer) {
    buffer.writeInt(animation.getNodeIndex());
    buffer.writeLong(animation.getDuration());
    PacketUtils.writeTransform(animation.getFrom(), buffer);
    PacketUtils.writeTransform(animation.getTo(), buffer);
  }

}
