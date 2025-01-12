package io.github.singlerr.admin.network.handler;

import io.github.singlerr.admin.network.PacketGameInfo;
import io.github.singlerr.admin.network.PacketPlayerInfoFragment;
import io.github.singlerr.admin.network.PacketRequestInfo;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.context.GameHistory;
import io.github.singlerr.sg.core.context.GameHistoryRecorder;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.Gender;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketHandler;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Slf4j
public final class PacketRequestInfoHandler implements PacketHandler<PacketRequestInfo> {

  public static final int SEGMENT_SIZE = 10;

  private static final ExecutorService PACKET_EXECUTOR =
      Executors.newFixedThreadPool(Runtime.getRuntime()
          .availableProcessors() * 10);
  private static final Set<UUID> pending = Collections.synchronizedSet(new HashSet<>());
  private static final AtomicInteger packetId = new AtomicInteger(0);
  private static final int THRESHOLD = 50;

  @Override
  public void handle(PacketRequestInfo packet, Player player) {
    if (pending.contains(player.getUniqueId())) {
      return;
    }

    pending.add(player.getUniqueId());
    final int pktId = packetId.getAndIncrement();
    if (pktId > THRESHOLD) {
      packetId.set(0);
    }

    PACKET_EXECUTOR.submit(() -> {
      try {
        NetworkRegistry network =
            Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();

        GameLifecycle.GameInfo currentGame =
            GameCore.getInstance().getCoreLifecycle().getCurrentGame();
        Collection<GameHistory> histories = GameHistoryRecorder.getAll();
        String id = currentGame != null ? currentGame.id() : "";
        PacketGameInfo infoPacket;

        if (currentGame == null) {
          infoPacket =
              new PacketGameInfo(id, new GameHistory(id, 0, 0), new ArrayList<>(histories));

          PacketPlayerInfoFragment playerInfoPacket =
              new PacketPlayerInfoFragment(pktId, id, new ArrayList<>(), 0, 0);
          network.getChannel().sendTo(player, infoPacket);
          network.getChannel().sendTo(player, playerInfoPacket);
          pending.remove(player.getUniqueId());
          return;
        }
        int currentSize = currentGame.context().getPlayerMap().size();
        infoPacket = new PacketGameInfo(id, new GameHistory(id,
            currentGame.context().getInitialPlayerSize() < currentSize ? currentSize :
                currentGame.context().getInitialPlayerSize(), currentSize),
            new ArrayList<>(histories));

        network.getChannel().sendTo(player, infoPacket);
        dividePlayers(pktId, id, new ArrayList<>(currentGame.context().getPlayers()), network,
            player);
        pending.remove(player.getUniqueId());
      } catch (Exception e) {
        pending.remove(player.getUniqueId());
        log.error("Error occurred while handling packet", e);
      }
    });
  }

  private void dividePlayers(int packetId, String id, List<GamePlayer> players,
                             NetworkRegistry networkRegistry,
                             Player p) {
    int count = 0;
    int index = 0;
    int playerSize = players.size();
    int packetCount = players.size() / SEGMENT_SIZE + 1;
    do {
      List<GamePlayer> sub = players.subList(count,
          Math.min(count + SEGMENT_SIZE, playerSize));
      PacketPlayerInfoFragment pkt =
          new PacketPlayerInfoFragment(packetId, id, sub, index++, packetCount);
      networkRegistry.getChannel().sendTo(p, pkt);
      count += sub.size();
    } while (count < playerSize);
  }

  private List<GameHistory> generateRandom(int c) {
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
          Component.text(id.toString()), Gender.MALE, 0));
    }
    return players;
  }
}
