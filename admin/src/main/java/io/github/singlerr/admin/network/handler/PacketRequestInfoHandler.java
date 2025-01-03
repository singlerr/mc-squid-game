package io.github.singlerr.admin.network.handler;

import io.github.singlerr.admin.network.PacketGameInfo;
import io.github.singlerr.admin.network.PacketPlayerInfoFragment;
import io.github.singlerr.admin.network.PacketRequestInfo;
import io.github.singlerr.sg.core.context.GameHistory;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketHandler;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PacketRequestInfoHandler implements PacketHandler<PacketRequestInfo> {

  public static final int SEGMENT_SIZE = 10;

  @Override
  public void handle(PacketRequestInfo packet, Player player) {
//    GameLifecycle.GameInfo currentGame = GameCore.getInstance().getCoreLifecycle().getCurrentGame();
//    Collection<GameHistory> histories = GameHistoryRecorder.getAll();
//    String id = currentGame != null ? currentGame.id() : "";
//    PacketGameInfo infoPacket = new PacketGameInfo(id, new ArrayList<>(histories));
//    PacketPlayerInfoFragment playerInfoPacket;
//    if (currentGame == null) {
//      playerInfoPacket = new PacketPlayerInfoFragment(id, new ArrayList<>());
//    } else {
//      playerInfoPacket =
//          new PacketPlayerInfoFragment(id, new ArrayList<>(currentGame.context().getPlayers()));
//    }

    String id = UUID.randomUUID().toString();
    PacketGameInfo infoPacket = new PacketGameInfo(id, new ArrayList<>(generateRandom(5)));
    PacketPlayerInfoFragment playerInfoPacket =
        new PacketPlayerInfoFragment(id, generateRandomGameInfo(200), false);
    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendTo(player, infoPacket);
//    network.getChannel().sendTo(player, playerInfoPacket);
    dividePlayers(id, generateRandomGameInfo(200), network, player);
  }

  private void dividePlayers(String id, List<GamePlayer> players, NetworkRegistry networkRegistry,
                             Player p) {
    int count = 0;
    do {
      List<GamePlayer> sub = players.subList(count,
          Math.min(count + SEGMENT_SIZE - 1, players.size()));
      boolean last = count + SEGMENT_SIZE >= players.size();
      PacketPlayerInfoFragment pkt = new PacketPlayerInfoFragment(id, sub, last);
      networkRegistry.getChannel().sendTo(p, pkt);
      count += SEGMENT_SIZE;
    } while (count < players.size());
  }

  private Collection<GameHistory> generateRandom(int c) {
    List<GameHistory> t = new ArrayList<>();
    SecureRandom r = new SecureRandom();
    for (int i = 0; i < c; i++) {
      t.add(new GameHistory(UUID.randomUUID().toString(), r.nextInt(160)));
    }
    return t;
  }

  private List<GamePlayer> generateRandomGameInfo(int c) {
    List<GamePlayer> players = new ArrayList<>();
    for (int i = 0; i < c; i++) {
      UUID id = UUID.randomUUID();
      players.add(new GamePlayer(id, null, GameRole.USER, Component.text(id.toString()),
          Component.text(id.toString())));
    }
    return players;
  }
}
