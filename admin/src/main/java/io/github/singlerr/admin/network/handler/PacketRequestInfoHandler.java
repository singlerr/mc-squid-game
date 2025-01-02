package io.github.singlerr.admin.network.handler;

import io.github.singlerr.admin.network.PacketGameInfo;
import io.github.singlerr.admin.network.PacketPlayerInfoFragment;
import io.github.singlerr.admin.network.PacketRequestInfo;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.context.GameHistory;
import io.github.singlerr.sg.core.context.GameHistoryRecorder;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.PacketHandler;
import java.util.ArrayList;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PacketRequestInfoHandler implements PacketHandler<PacketRequestInfo> {

  @Override
  public void handle(PacketRequestInfo packet, Player player) {
    GameLifecycle.GameInfo currentGame = GameCore.getInstance().getCoreLifecycle().getCurrentGame();
    Collection<GameHistory> histories = GameHistoryRecorder.getAll();
    String id = currentGame != null ? currentGame.id() : "";
    PacketGameInfo infoPacket = new PacketGameInfo(id, new ArrayList<>(histories));
    PacketPlayerInfoFragment playerInfoPacket;
    if (currentGame == null) {
      playerInfoPacket = new PacketPlayerInfoFragment(id, new ArrayList<>());
    } else {
      playerInfoPacket =
          new PacketPlayerInfoFragment(id, new ArrayList<>(currentGame.context().getPlayers()));
    }

    NetworkRegistry network =
        Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendTo(player, infoPacket);
    network.getChannel().sendTo(player, playerInfoPacket);
  }
}
