package io.github.singlerr.dalgona.network.handler;

import io.github.singlerr.dalgona.game.DalgonaGame;
import io.github.singlerr.dalgona.network.PacketDalgonaResult;
import io.github.singlerr.sg.core.network.PacketHandler;
import org.bukkit.entity.Player;

public final class PacketDalgonaResultHandler implements PacketHandler<PacketDalgonaResult> {

  private final DalgonaGame game;

  public PacketDalgonaResultHandler(DalgonaGame game) {
    this.game = game;
  }

  @Override
  public void handle(PacketDalgonaResult packet, Player player) {
    game.getContext().handleResult(player, packet);
  }
}
