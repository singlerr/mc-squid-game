package io.github.singlerr.admin.network;

import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.network.Packet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.FriendlyByteBuf;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PacketPlayerInfoFragment implements Packet {

  public static final String ID = "sgadmin:player_info";

  private int packetId;
  private String id;
  private List<GamePlayer> playerList;
  private int packetIndex;
  private int packetCount;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeInt(packetCount);
    buffer.writeInt(packetId);
    buffer.writeUtf(id);
    buffer.writeInt(packetIndex);
    buffer.writeInt(playerList.size());
    for (GamePlayer p : playerList) {
      writeGamePlayer(p, buffer);
    }
  }

  private void writeGamePlayer(GamePlayer player, FriendlyByteBuf buffer) {
    buffer.writeUUID(player.getId());
    buffer.writeUtf(
        PlainTextComponentSerializer.plainText().serialize(player.getAdminDisplayName()));
    buffer.writeUtf(player.getRole().toString());
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {

  }
}
