package io.github.singlerr.admin.network;

import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.network.Packet;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class PacketPlayerInfoFragment implements Packet {

  public static final String ID = "sgadmin:player_info";

  private String id;
  private List<GamePlayer> playerList;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeUtf(id);
    buffer.writeInt(playerList.size());
    for (GamePlayer p : playerList) {
      writeGamePlayer(p, buffer);
    }
  }

  private void writeGamePlayer(GamePlayer player, FriendlyByteBuf buffer) {
    buffer.writeUUID(player.getId());
    buffer.writeUtf(player.getUserDisplayName().toString());
    buffer.writeUtf(player.getRole().toString());
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {

  }
}
