package io.github.singlerr.sg.core.context;

import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.potion.PotionEffectType;

@Getter
public class GameContext {

  private final Map<UUID, GamePlayer> players;
  @Getter(value = AccessLevel.NONE)
  private final GameEventBus eventBus;
  private final GameSettings settings;
  @Getter
  private final int initialPlayerSize;
  @Setter
  private GameStatus status;
  @Getter
  @Setter
  private String id;

  public GameContext(Map<UUID, GamePlayer> players, GameStatus status, GameEventBus eventBus,
                     GameSettings settings) {
    this.players = new HashMap<>(players);
    this.status = status;
    this.eventBus = eventBus;
    this.settings = settings;
    this.initialPlayerSize = players.size();
  }

  public Map<UUID, GamePlayer> getPlayerMap() {
    return players;
  }

  public Collection<GamePlayer> getPlayers() {
    return players.values();
  }

  public Collection<GamePlayer> getPlayers(int level) {
    return getPlayers().stream().filter(p -> p.getRole().getLevel() <= level).toList();
  }

  public Collection<GamePlayer> getPlayers(GameRole role) {
    return getPlayers().stream().filter(p -> p.getRole() == role).toList();
  }

  public boolean kickPlayer(GamePlayer player) {
    if (!player.available()) {
      players.remove(player.getId());
      return true;
    }
    if (players.containsKey(player.getId())) {
      if (player.getRole() == GameRole.TROY) {
        player.setRole(GameRole.ADMIN);
        player.getPlayer().addPotionEffect(PotionEffectType.INVISIBILITY.createEffect(9999, 1));
        player.getPlayer().addPotionEffect(PotionEffectType.BLINDNESS.createEffect(9999, 1));
        player.getPlayer().setGameMode(GameMode.CREATIVE);
      } else {
        players.remove(player.getId());
        eventBus.postGameExit(this, player);
      }
      return true;
    }

    return false;
  }

  public boolean joinPlayer(GamePlayer player) {
    if (!player.available()) {
      return true;
    }
    if (!players.containsKey(player.getId())) {
      players.put(player.getId(), player);
      eventBus.postGameJoin(this, player);
      return true;
    }
    return false;
  }

  public void broadcast(Component component, GameRole role) {
    getPlayers().stream().filter(p -> p.getRole() == role)
        .forEach(p -> p.sendMessage(component));
  }

  public void assignNumberName(GamePlayer player) {
    player.setUserDisplayName(Component.text(getPlayers().size()));
    player.setAdminDisplayName(Component.text("[").append(player.getUserDisplayName())
        .append(Component.text("]").append(Component.text(player.getPlayer().getName()))));
  }

  public GamePlayer getPlayer(UUID playerId) {
    return players.get(playerId);
  }

  public void syncNameLowerThan(int level, GamePlayer target) {
    syncName(p -> p.getRole().getLevel() <= level, target);
  }

  public void syncName(GameRole role, GamePlayer target) {
    syncName(p -> p.getRole() == role, target);
  }

  public void syncName(Predicate<GamePlayer> filter, GamePlayer target) {
    syncName(getPlayers().stream().filter(filter).toList(), target);
  }

  public void syncName(Collection<GamePlayer> players, GamePlayer target) {
    if (!target.available()) {
      return;
    }
    boolean admin = target.getRole().getLevel() >= GameRole.ADMIN.getLevel();

    for (GamePlayer player : players) {
      if (!player.available()) {
        continue;
      }
      ServerPlayer handle = ((CraftPlayer) player.getPlayer()).getHandle();
      ClientboundPlayerInfoUpdatePacket pkt = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
          ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
          new ClientboundPlayerInfoUpdatePacket.Entry(handle.getUUID(), handle.getGameProfile(),
              true, handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
              net.minecraft.network.chat.Component.literal(
                  admin ? player.getAdminDisplayName().toString() :
                      player.getUserDisplayName().toString()),
              Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(pkt);
    }
  }

  public void syncNameLowerThan(GamePlayer player, int level) {
    syncName(player, p -> p.getRole().getLevel() <= level);
  }


  public void syncName(GamePlayer player, GameRole role) {
    syncName(player, p -> p.getRole() == role);
  }

  public void syncName(GamePlayer player, Predicate<GamePlayer> filter) {
    syncName(player, getPlayers().stream().filter(filter).toList());
  }

  public void syncName(GamePlayer player, Collection<GamePlayer> targets) {
    if (!player.available()) {
      return;
    }
    ServerPlayer handle = ((CraftPlayer) player.getPlayer()).getHandle();
    for (GamePlayer target : targets) {
      if (!target.available()) {
        continue;
      }
      ClientboundPlayerInfoUpdatePacket pkt = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
          ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME,
          ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LATENCY,
          ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED),
          new ClientboundPlayerInfoUpdatePacket.Entry(handle.getUUID(), handle.getGameProfile(),
              true,
              handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
              net.minecraft.network.chat.Component.literal(
                  target.getRole().getLevel() >= GameRole.ADMIN.getLevel() ?
                      player.getAdminDisplayName().toString() :
                      player.getUserDisplayName().toString()),
              Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(pkt);
    }
  }
}
