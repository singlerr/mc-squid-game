package io.github.singlerr.sg.core.context;

import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.GameMode;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

@Slf4j
@Getter
public class GameContext {

  private static AtomicInteger idCount = new AtomicInteger(0);
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
    this.initialPlayerSize = getPlayers(GameRole.TROY.getLevel()).size();
  }

  public Map<UUID, GamePlayer> getPlayerMap() {
    return players;
  }

  public Collection<GamePlayer> getPlayers() {
    return players.values();
  }

  public List<GamePlayer> getPlayers(int level) {
    return getPlayers().stream().filter(p -> p.getRole().getLevel() <= level).toList();
  }

  public Collection<GamePlayer> getPlayers(GameRole role) {
    return getPlayers().stream().filter(p -> p.getRole() == role).toList();
  }

  public Collection<Player> getOnlinePlayers(GameRole role) {
    return getPlayers().stream().filter(p -> p.getRole() == role).filter(GamePlayer::available)
        .map(GamePlayer::getPlayer).toList();
  }

  public Collection<Player> getOnlinePlayers(int level) {
    return getPlayers().stream().filter(p -> p.getRole().getLevel() <= level)
        .filter(GamePlayer::available).map(GamePlayer::getPlayer).toList();
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
        PlayerUtils.changeSkin(player.getPlayer(), GameCore.getInstance().getAdminSkinUrl(), false);
      } else {
        players.remove(player.getId());
        eventBus.postGameExit(this, player);
      }

      return true;
    }

    return false;
  }

  public void tryBanPlayer(GamePlayer player) {
    if (!player.available()) {
      return;
    }

    if (!GameCore.getInstance().shouldBan()) {
      return;
    }

    if (player.getRole().getLevel() < GameRole.TROY.getLevel()) {
      player.getPlayer().ban("오징어 게임에서 탈락했습니다!", (Date) null, null, false);
    }
  }

  public boolean joinPlayer(GamePlayer player) {
    if (!player.available()) {
      return true;
    }
    if (!players.containsKey(player.getId())) {
      players.put(player.getId(), player);
      eventBus.postGameJoin(this, player);
      return true;
    } else {
      if (player.getRole() == GameRole.TROY) {
        GamePlayer original = players.get(player.getId());
        if (original != null) {
          original.setRole(GameRole.TROY);
        } else {
          players.put(player.getId(), player);
          eventBus.postGameJoin(this, player);
        }
        return true;
      }

    }
    return false;
  }

  public void broadcast(Component component, GameRole role) {
    getPlayers().stream().filter(p -> p.getRole() == role)
        .forEach(p -> p.sendMessage(component));
  }

  public void assignNumberName(GamePlayer player) {
    int num = idCount.incrementAndGet();
    player.setUserDisplayName(Component.text(num));
    player.setAdminDisplayName(Component.text("[").append(player.getUserDisplayName())
        .append(Component.text("]").append(Component.text(player.getPlayer().getName()))));
    player.setUserNumber(num);
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
      if (target.getId().equals(player.getId())) {
        continue;
      }

      ServerPlayer handle = ((CraftPlayer) player.getPlayer()).getHandle();
      String newName = admin ? "[" + player.getUserNumber() + "]" + player.getPlayer().getName() :
          String.valueOf(player.getUserNumber());

      final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumSet =
          EnumSet.of(
              ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
              ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);
      final List<ClientboundPlayerInfoUpdatePacket.Entry> entry =
          List.of(new ClientboundPlayerInfoUpdatePacket.Entry(handle.getGameProfile().getId(),
              handle.getGameProfile(),
              true,
              handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
              net.minecraft.network.chat.Component.literal(newName),
              Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
      ClientboundBundlePacket bundlePacket =
          new ClientboundBundlePacket(List.of(new ClientboundPlayerInfoUpdatePacket(enumSet, entry),
              PlayerUtils.createChangeCustomNamePacket(player.getPlayer(), newName)));

      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(bundlePacket);
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
      if (target.getId().equals(player.getId())) {
        continue;
      }

      boolean admin = target.getRole().getLevel() >= GameRole.ADMIN.getLevel();
      String newName = admin ? "[" + player.getUserNumber() + "]" + player.getPlayer().getName() :
          String.valueOf(player.getUserNumber());

      final EnumSet<ClientboundPlayerInfoUpdatePacket.Action> enumSet =
          EnumSet.of(
              ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED,
              ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME);

      final List<ClientboundPlayerInfoUpdatePacket.Entry> entry =
          List.of(
              new ClientboundPlayerInfoUpdatePacket.Entry(handle.getUUID(), handle.getGameProfile(),
                  true,
                  handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
                  net.minecraft.network.chat.Component.literal(newName),
                  Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
      ClientboundBundlePacket bundlePacket =
          new ClientboundBundlePacket(List.of(new ClientboundPlayerInfoUpdatePacket(enumSet, entry),
              PlayerUtils.createChangeCustomNamePacket(player.getPlayer(), newName)));
      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(
          bundlePacket);
    }
  }
}
