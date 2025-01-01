package io.github.singlerr.dalgona.game;

import io.github.singlerr.dalgona.network.PacketDalgonaRequest;
import io.github.singlerr.dalgona.network.PacketDalgonaResult;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DalgonaGameContext extends GameContext {

  private final Map<UUID, PlayerDalgonaStatus> status;
  @Getter
  @Setter
  private DalgonaGameStatus gameStatus;
  @Getter
  @Setter
  private String currentDalgonaId;

  @Getter
  @Setter
  private long startTime;

  public DalgonaGameContext(List<GamePlayer> players,
                            GameStatus status,
                            GameEventBus eventBus,
                            GameSettings settings) {
    super(players, status, eventBus, settings);
    this.status = new HashMap<>();
  }

  public DalgonaGameSettings getGameSettings() {
    return (DalgonaGameSettings) getSettings();
  }

  public void handleResult(Player player, PacketDalgonaResult packet) {
    if (gameStatus != DalgonaGameStatus.PROGRESS) {
      return;
    }
    GamePlayer p = getPlayer(player.getUniqueId());
    if (p == null) {
      return;
    }

    status.computeIfPresent(player.getUniqueId(),
        (id, t) -> packet.isSuccess() ? PlayerDalgonaStatus.SUCCESS : PlayerDalgonaStatus.FAILURE);
    if (!packet.isSuccess()) {
      broadcast(
          Component.text("플레이어 ").append(p.getAdminDisplayName()).append(Component.text(" 달고나 실패"))
              .style(
                  Style.style(NamedTextColor.RED)), GameRole.ADMIN);
    }
  }

  public PlayerDalgonaStatus getStatus(UUID id) {
    return status.get(id);
  }

  public void startGame() {
    setGameStatus(DalgonaGameStatus.PROGRESS);
    setStartTime(System.currentTimeMillis());
  }

  public void provideDalgona() {
    ItemStack dalgonaItem = new ItemStack(getGameSettings().getDalgonaType());
    ItemMeta meta = dalgonaItem.getItemMeta();
    meta.displayName(Component.text("달고나").style(Style.style(NamedTextColor.YELLOW)));

    for (GamePlayer player : getPlayers()) {
      if (player.getRole() == GameRole.USER) {
        player.getPlayer().getInventory().addItem(dalgonaItem);
      }
    }
  }

  public void beginDalgona(Player player) {
    status.put(player.getUniqueId(), PlayerDalgonaStatus.IDLE);
    NetworkRegistry
        network = Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendTo(player, new PacketDalgonaRequest(currentDalgonaId));
  }

  public void end() {
    List<GamePlayer> failedPlayers = status.entrySet().stream()
        .filter(e -> getPlayer(e.getKey()) != null && e.getValue() != PlayerDalgonaStatus.SUCCESS)
        .map(e -> getPlayer(e.getKey())).toList();
    Component msg = Component.text("실패자: [");
    for (int i = 0; i < failedPlayers.size(); i++) {
      GamePlayer p = failedPlayers.get(i);
      if (i != failedPlayers.size() - 1) {
        msg = msg.append(p.getAdminDisplayName()).append(Component.text(","));
      } else {
        msg = msg.append(p.getAdminDisplayName());
      }
    }

    msg = msg.append(Component.text("]")).style(Style.style(NamedTextColor.YELLOW));
    broadcast(msg, GameRole.ADMIN);
  }

}
