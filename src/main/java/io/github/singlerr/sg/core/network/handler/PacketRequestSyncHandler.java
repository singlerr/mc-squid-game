package io.github.singlerr.sg.core.network.handler;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.network.PacketHandler;
import io.github.singlerr.sg.core.network.packets.PacketRequestSync;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;

@Slf4j
@RequiredArgsConstructor
public final class PacketRequestSyncHandler implements PacketHandler<PacketRequestSync> {

  private final GameCore instance;

  @Override
  public void handle(PacketRequestSync packet, Player player) {
    if (instance.getCoreLifecycle() == null) {
      return;
    }

    if (instance.getCoreLifecycle().getCurrentGame() == null) {
      return;
    }

    GameLifecycle.GameInfo info = instance.getCoreLifecycle().getCurrentGame();
    if (info.context() == null) {
      return;
    }

    GameContext context = info.context();
    GamePlayer targetPlayer = context.getPlayer(packet.getPlayerId());
    if (targetPlayer == null) {
      return;
    }
    if (!targetPlayer.available()) {
      return;
    }

    GamePlayer self = context.getPlayer(player.getUniqueId());
    if (self == null) {
      return;
    }
    if (!self.available()) {
      return;
    }

    context.syncName(Collections.singleton(self), targetPlayer);
    context.syncName(Collections.singleton(targetPlayer), self);
  }
}
