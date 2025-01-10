package io.github.singlerr.dalgona.network;

import io.github.singlerr.sg.core.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PacketDalgonaRequest implements Packet {

  public static final String ID = "intermission:dalgona_request";

  private String dalgonaImagePath;
  private int threshold;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeUtf(dalgonaImagePath);
    buffer.writeInt(threshold);
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    dalgonaImagePath = buffer.readUtf();
    threshold = buffer.readInt();
  }
}
