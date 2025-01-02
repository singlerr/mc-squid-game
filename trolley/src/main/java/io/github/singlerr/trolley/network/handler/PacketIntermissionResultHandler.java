package io.github.singlerr.trolley.network.handler;

import io.github.singlerr.sg.core.network.PacketHandler;
import io.github.singlerr.trolley.game.Ticker;
import io.github.singlerr.trolley.game.TrolleyGame;
import io.github.singlerr.trolley.network.PacketIntermissionResult;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public final class PacketIntermissionResultHandler
    implements PacketHandler<PacketIntermissionResult> {

  private final TrolleyGame game;

  @Override
  public void handle(PacketIntermissionResult packet, Player player) {
    Ticker ticker = game.getContext().getPlayerStatus(player.getUniqueId());
    if (ticker == null) {
      return;
    }

    ticker.handleIntermissionResult(packet);
  }
}
