package io.github.singlerr.mgr.listener;

import io.github.singlerr.mgr.game.MGRGame;
import io.github.singlerr.mgr.game.MGRGameContext;
import io.github.singlerr.mgr.game.MGRGameStatus;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.utils.InteractableListener;
import io.github.singlerr.sg.core.utils.Region;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

@Slf4j
@RequiredArgsConstructor
public final class MGREventListener extends InteractableListener {

  private final MGRGame game;

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
    if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    if (event.getHand() != EquipmentSlot.HAND) {
      return;
    }

    Block block = event.getClickedBlock();
    if (block == null) {
      return;
    }
    MGRGameContext context = game.getContext();
    GamePlayer gamePlayer = context.getPlayer(event.getPlayer().getUniqueId());
    if (gamePlayer == null) {
      return;
    }
    if (block.getBlockData() instanceof Openable door) {
      if (gamePlayer.getRole() == GameRole.ADMIN) {

        boolean open = !door.isOpen();
        door.setOpen(open);
        block.setBlockData(door);
        block.getState().update(true);
        block.getWorld().playSound(block.getLocation(),
            open ? Sound.BLOCK_IRON_DOOR_OPEN : Sound.BLOCK_IRON_DOOR_CLOSE, 1f, 1f);
        return;
      }
    }
    if (block.getBlockData() instanceof Powerable) {
      if (gamePlayer.getRole().getLevel() <= GameRole.TROY.getLevel() &&
          context.getGameStatus() == MGRGameStatus.CLOSING_ROOM) {
        event.setCancelled(true);
      }
    }
  }


  public void onMount(EntityDismountEvent event) {
    if (!(event.getEntity() instanceof Player p)) {
      return;
    }

    if (event.getDismounted().getType() != EntityType.INTERACTION) {
      return;
    }

    if (game.getContext().getGameStatus() != MGRGameStatus.PLAYING_MUSIC) {
      return;
    }

    GamePlayer player = game.getContext().getPlayer(p.getUniqueId());
    if (player == null) {
      return;
    }

    if (player.getRole().getLevel() >= GameRole.ADMIN.getLevel()) {
      return;
    }

    event.setCancelled(true);
  }

  @EventHandler
  public void onMove(PlayerMoveEvent event) {
    MGRGameContext context = game.getContext();
    GamePlayer gamePlayer = context.getPlayer(event.getPlayer().getUniqueId());
    if (gamePlayer == null) {
      return;
    }

    if (gamePlayer.getRole().getLevel() > GameRole.TROY.getLevel()) {
      return;
    }

    if (context.getGameStatus() != MGRGameStatus.JOINING_ROOM) {
      return;
    }

    Location to = event.getTo();
    Location from = event.getFrom();

    for (Map.Entry<Integer, Region> entry : context.getGameSettings().getRooms()
        .entrySet()) {
      int roomNum = entry.getKey();
      Region r = entry.getValue();
      // check in -> in;
      if (r.isIn(to) && r.isIn(from)) {
        continue;
      }
      // check in -> out
      if (!r.isIn(to) && r.isIn(from)) {
        AtomicInteger count = context.getPlayerCounts().get(roomNum);
        count.decrementAndGet();
        continue;
      }
      // check out -> in
      if (r.isIn(to) && !r.isIn(from)) {
        AtomicInteger count = context.getPlayerCounts().get(roomNum);
        if (count.incrementAndGet() >= context.getPlayerCount()) {
          Location loc = context.getGameSettings().getDoors().get(roomNum);
          context.setDoorOpen(loc.getBlock(), false);
        }
        continue;
      }
    }
  }


  @EventHandler
  public void onQuit(PlayerDeathEvent event) {
    Player player = event.getPlayer();
    MGRGameContext context = game.getContext();
    GamePlayer gamePlayer;
    if ((gamePlayer = context.getPlayer(player.getUniqueId())) != null) {
      context.kickPlayer(gamePlayer);
    }
  }

  @EventHandler
  public void removeDeathMessage(PlayerDeathEvent event) {
    event.deathMessage(null);
  }


}
