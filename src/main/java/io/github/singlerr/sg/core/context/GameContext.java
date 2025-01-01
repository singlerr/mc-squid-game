package io.github.singlerr.sg.core.context;

import com.google.common.collect.Lists;
import io.github.singlerr.sg.core.setup.GameSettings;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
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
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;

@Getter
public class GameContext {

  private final List<GamePlayer> players;
  @Getter(value = AccessLevel.NONE)
  private final GameEventBus eventBus;
  private final GameSettings settings;
  @Setter
  private GameStatus status;

  public GameContext(List<GamePlayer> players, GameStatus status, GameEventBus eventBus,
                     GameSettings settings) {
    this.players = Lists.newArrayList(players);
    this.status = status;
    this.eventBus = eventBus;
    this.settings = settings;
  }

  public boolean kickPlayer(GamePlayer player) {
    if (PlayerUtils.contains(players, player)) {
      players.remove(player);
      eventBus.postGameExit(this, player);
      return true;
    }

    return false;
  }

  public boolean joinPlayer(GamePlayer player) {
    if (!PlayerUtils.contains(players, player)) {
      players.add(player);
      eventBus.postGameJoin(this, player);
      return true;
    }
    return false;
  }

  public void broadcast(Component component, GameRole role) {
    getPlayers().stream().filter(p -> p.getRole() == role)
        .forEach(p -> p.getPlayer().sendMessage(component));
  }

  public void assignNumberName(GamePlayer player) {
    player.setUserDisplayName(Component.text(getPlayers().size()));
    player.setAdminDisplayName(Component.text("[").append(player.getUserDisplayName())
        .append(Component.text("]").append(Component.text(player.getPlayer().getName()))));
  }

  public GamePlayer getPlayer(UUID playerId) {
    return players.stream().filter(p -> p.getPlayer().getUniqueId().equals(playerId)).findAny()
        .orElse(null);
  }

  public void syncName(GameRole role, GamePlayer target) {
    syncName(p -> p.getRole() == role, target);
  }

  public void syncName(Predicate<GamePlayer> filter, GamePlayer target) {
    syncName(getPlayers().stream().filter(filter).toList(), target);
  }

  public void syncName(Collection<GamePlayer> players, GamePlayer target) {
    for (GamePlayer player : players) {
      ServerPlayer handle = ((CraftPlayer) player.getPlayer()).getHandle();
      ClientboundPlayerInfoUpdatePacket pkt = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
          ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
          new ClientboundPlayerInfoUpdatePacket.Entry(handle.getUUID(), handle.getGameProfile(),
              true, handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
              net.minecraft.network.chat.Component.literal(player.getAdminDisplayName().toString()),
              Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(pkt);
    }
  }

  public void syncName(GamePlayer player, GameRole role) {
    syncName(player, p -> p.getRole() == role);
  }

  public void syncName(GamePlayer player, Predicate<GamePlayer> filter) {
    syncName(player, getPlayers().stream().filter(filter).toList());
  }

  public void syncName(GamePlayer player, Collection<GamePlayer> targets) {
    ServerPlayer handle = ((CraftPlayer) player.getPlayer()).getHandle();
    ClientboundPlayerInfoUpdatePacket pkt = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(
        ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME),
        new ClientboundPlayerInfoUpdatePacket.Entry(handle.getUUID(), handle.getGameProfile(), true,
            handle.connection.latency(), handle.gameMode.getGameModeForPlayer(),
            net.minecraft.network.chat.Component.literal(player.getAdminDisplayName().toString()),
            Optionull.map(handle.getChatSession(), RemoteChatSession::asData)));
    for (GamePlayer target : targets) {
      ((CraftPlayer) target.getPlayer()).getHandle().connection.send(pkt);
    }
  }
}
