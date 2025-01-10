package io.github.singlerr.sg.core.network.packets;

import io.github.singlerr.sg.core.network.Packet;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@Data
@AllArgsConstructor
@NoArgsConstructor
public final class PacketRequestSync implements Packet {

  public static final String ID = "intermission:request_sync";

  private UUID playerId;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeUUID(playerId);
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    playerId = buffer.readUUID();
  }
}
