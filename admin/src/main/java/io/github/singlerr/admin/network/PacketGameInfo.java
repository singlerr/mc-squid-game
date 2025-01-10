package io.github.singlerr.admin.network;

import io.github.singlerr.sg.core.context.GameHistory;
import io.github.singlerr.sg.core.network.Packet;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@NoArgsConstructor
@AllArgsConstructor
@Data
public final class PacketGameInfo implements Packet {

  public static final String ID = "sgadmin:game_info";

  private String currentGameId;
  private GameHistory currentGameInfo;
  private List<GameHistory> games;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeUtf(currentGameId);
    writeGame(currentGameInfo, buffer);
    buffer.writeInt(games.size());
    for (GameHistory game : games) {
      writeGame(game, buffer);
    }
  }

  private void writeGame(GameHistory game, FriendlyByteBuf buffer) {
    buffer.writeUtf(game.getId());
    buffer.writeInt(game.getPlayerCount());
    buffer.writeInt(game.getSurvivorsCount());
  }

  private GameHistory readGame(FriendlyByteBuf buffer) {
    return new GameHistory(buffer.readUtf(), buffer.readInt(), buffer.readInt());
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    currentGameId = buffer.readUtf();
    currentGameInfo = readGame(buffer);
    int size = buffer.readInt();
    List<GameHistory> games = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      games.add(readGame(buffer));
    }
    this.games = games;
  }
}
