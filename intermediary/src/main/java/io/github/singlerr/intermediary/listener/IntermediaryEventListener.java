package io.github.singlerr.intermediary.listener;

import io.github.singlerr.intermediary.game.IntermediaryGameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.utils.InteractableListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class IntermediaryEventListener extends InteractableListener {

  private final IntermediaryGameContext context;

  public IntermediaryEventListener(IntermediaryGameContext context) {
    this.context = context;
  }

  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getPlayer(player.getUniqueId())) != null) {
      context.kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getPlayer(player.getUniqueId())) != null) {
      context.kickPlayer(gamePlayer);
    }
  }
}
