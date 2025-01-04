package io.github.singlerr.trolley.listener;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.trolley.game.Ticker;
import io.github.singlerr.trolley.game.TrolleyGame;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

@RequiredArgsConstructor
public final class TrolleyEventListener extends InteractableListener {

  private final TrolleyGame game;

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    GamePlayer p = game.getContext().getPlayer(event.getPlayer().getUniqueId());
    if (p == null) {
      return;
    }

    game.getContext().kickPlayer(p);
  }

  @EventHandler
  public void onJump(PlayerJumpEvent event) {
    GamePlayer p = game.getContext().getPlayer(event.getPlayer().getUniqueId());
    if (p == null) {
      return;
    }

    Ticker t = game.getContext().getPlayerStatus(event.getPlayer().getUniqueId());
    if (t == null) {
      return;
    }

    if (!t.shouldTick()) {
      return;
    }

    event.setCancelled(true);
  }
}
