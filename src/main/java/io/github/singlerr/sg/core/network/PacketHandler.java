package io.github.singlerr.sg.core.network;

import org.bukkit.entity.Player;

public interface PacketHandler<T extends Packet> {

  void handle(T packet, Player player);

}
