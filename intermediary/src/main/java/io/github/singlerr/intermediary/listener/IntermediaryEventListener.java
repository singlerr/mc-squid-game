package io.github.singlerr.intermediary.listener;

import io.github.singlerr.intermediary.game.IntermediaryGame;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.utils.InteractableListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class IntermediaryEventListener extends InteractableListener {

  private final IntermediaryGame context;

  public IntermediaryEventListener(IntermediaryGame context) {
    this.context = context;
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getContext().getPlayer(player.getUniqueId())) != null) {
      context.getContext().kickPlayer(gamePlayer);
    }
  }

}
