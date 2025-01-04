package io.github.singlerr.roulette.listener;

import io.github.singlerr.roulette.game.Gun;
import io.github.singlerr.roulette.game.RouletteGame;
import io.github.singlerr.roulette.game.RouletteGameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public final class RouletteEventListener extends InteractableListener {

  private final RouletteGame game;

  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    RouletteGameContext context = game.getContext();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getPlayer(player.getUniqueId())) != null) {
      context.kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void onInteract(PlayerInteractEvent event) {
    if (!event.getAction().isRightClick()) {
      return;
    }

    ItemStack item = event.getItem();
    if (item == null) {
      return;
    }

    if (!event.getPlayer().isSneaking()) {
      return;
    }

    GamePlayer p = game.getContext().getPlayer(event.getPlayer().getUniqueId());
    if (p == null) {
      return;
    }

    if (p.getRole().getLevel() < GameRole.ADMIN.getLevel()) {
      return;
    }

    Gun gun = game.getContext().getGun(item);
    if (gun == null) {
      return;
    }

    if (!p.available()) {
      return;
    }

    infoCallback(p.getPlayer(), "리볼버 재장전 - 총 {}발, 진탄환 {}발", gun.getBulletAmount(),
        gun.getRealBulletAmount());
    gun.reload(gun.getBulletAmount(), gun.getRealBulletAmount());
    game.getContext().update(item, gun);
    game.getContext().playReloadingAnimation(p.getPlayer());
  }

  @EventHandler
  public void onShoot(ProjectileLaunchEvent event) {
    if (!(event.getEntity().getShooter() instanceof Player player)) {
      return;
    }

    ItemStack item = player.getInventory().getItem(player.getActiveItemHand());
    if (item == null) {
      return;
    }
    GamePlayer p = game.getContext().getPlayer(player.getUniqueId());
    if (p == null) {
      return;
    }
    if (p.getRole().getLevel() > GameRole.USER.getLevel()) {
      return;
    }

    Gun gun = game.getContext().getGun(item);
    if (gun == null) {
      return;
    }

    game.getContext().shot(player, gun);
    game.getContext().update(item, gun);
    event.setCancelled(true);
  }
}
