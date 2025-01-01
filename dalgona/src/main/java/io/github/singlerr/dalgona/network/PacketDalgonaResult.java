package io.github.singlerr.dalgona.network;

import io.github.singlerr.sg.core.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PacketDalgonaResult implements Packet {

  public static final String ID = "intermission:dalgona_result";

  private boolean success;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeBoolean(success);
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    success = buffer.readBoolean();
  }
}
