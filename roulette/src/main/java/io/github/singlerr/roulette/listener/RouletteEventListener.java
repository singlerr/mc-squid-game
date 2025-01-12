package io.github.singlerr.roulette.listener;

import io.github.singlerr.roulette.game.Gun;
import io.github.singlerr.roulette.game.RouletteGame;
import io.github.singlerr.roulette.game.RouletteGameContext;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.papermc.paper.ban.BanListType;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;

@RequiredArgsConstructor
public final class RouletteEventListener extends InteractableListener {

  private final RouletteGame game;

  @EventHandler
  public void onJoin(PlayerJoinEvent event) {
    if (game == null || game.getContext() == null) {
      return;
    }
    GameContext context = game.getContext();
    GamePlayer player = context.getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }
    Bukkit.getScheduler().scheduleSyncDelayedTask(GameCore.getInstance(), () -> {

      if (!player.available()) {
        return;
      }
      player.getPlayer().setCustomNameVisible(true);
      context.syncName(player, context.getPlayers());
      context.syncName(context.getPlayers(), player);
    }, 20L);
  }

  @EventHandler
  public void banPlayer(PlayerRespawnEvent event) {
    if (Bukkit.getServer().getBanList(BanListType.PROFILE)
        .isBanned(event.getPlayer().getPlayerProfile())) {
      event.getPlayer().kick(Component.text("오징어 게임에서 탈락했습니다!"));
    }
  }

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
  public void onChat(AsyncChatEvent event) {
    GameContext context = game.getContext();
    GamePlayer player = context.getPlayer(event.getPlayer().getUniqueId());
    if (player == null) {
      return;
    }

    for (GamePlayer p : context.getPlayers()) {
      if (!p.available()) {
        continue;
      }
      Component prefix =
          p.getRole().getLevel() >= GameRole.ADMIN.getLevel() ? player.getAdminDisplayName() :
              player.getUserDisplayName();
      p.sendMessage(prefix.append(Component.text(" : ")).append(event.message()));
    }

    event.setCancelled(true);
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
    event.setCancelled(true);
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
    event.setCancelled(true);
    if (item.getItemMeta() instanceof CrossbowMeta meta) {
      meta.setChargedProjectiles(new ArrayList<>());
      item.setItemMeta(meta);
    }

    game.getContext().shot(player, gun, item);
  }

  @EventHandler
  public void removeDeathMessage(PlayerDeathEvent event) {
    event.deathMessage(null);
  }

}
