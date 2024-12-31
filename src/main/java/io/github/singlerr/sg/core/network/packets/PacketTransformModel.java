package io.github.singlerr.sg.core.network.packets;

import io.github.singlerr.sg.core.network.Packet;
import io.github.singlerr.sg.core.utils.PacketUtils;
import io.github.singlerr.sg.core.utils.Transform;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;

@Getter
@AllArgsConstructor
public final class PacketTransformModel implements Packet {

  public static final String ID = "vanilla-gltf:transform_model";

  private UUID id;
  private int entityId;
  private Transform transform;

  public PacketTransformModel() {

  }

  @Override
  public void writePayload(FriendlyByteBuf friendlyByteBuf) {
    friendlyByteBuf.writeUUID(id);
    friendlyByteBuf.writeInt(entityId);
    PacketUtils.writeTransform(transform, friendlyByteBuf);
  }

  @Override
  public void readPayload(FriendlyByteBuf buf) {
    id = buf.readUUID();
    entityId = buf.readInt();
    transform = PacketUtils.readTransform(buf);
  }
}
