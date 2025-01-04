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
import java.security.SecureRandom;
import java.util.ArrayList;
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
  private final Map<UUID, String> dalgonaImages;
  private final SecureRandom random;
  @Getter
  @Setter
  private DalgonaGameStatus gameStatus;
  @Getter
  @Setter
  private long startTime;

  public DalgonaGameContext(Map<UUID, GamePlayer> players,
                            GameStatus status,
                            GameEventBus eventBus,

                            GameSettings settings) {
    super(players, status, eventBus, settings);
    this.status = new HashMap<>();
    this.dalgonaImages = new HashMap<>();
    this.random = new SecureRandom();
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
    List<String> dalgonas = new ArrayList<>(getGameSettings().getDalgonaImages().values());
    ItemStack dalgonaItem = new ItemStack(getGameSettings().getDalgonaType());
    ItemMeta meta = dalgonaItem.getItemMeta();
    meta.displayName(Component.text("달고나").style(Style.style(NamedTextColor.YELLOW)));
    getGameSettings().getDalgonaImages().values();
    for (GamePlayer player : getPlayers()) {
      if (!player.available()) {
        continue;
      }
      if (player.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        String dalgona = dalgonas.get(random.nextInt(dalgonas.size()));
        dalgonaImages.put(player.getPlayer().getUniqueId(), dalgona);
        status.put(player.getId(), PlayerDalgonaStatus.IDLE);
        player.getPlayer().getInventory().addItem(dalgonaItem);
      }
    }
  }

  public void beginDalgona(Player player) {
    if (!dalgonaImages.containsKey(player.getUniqueId())) {
      return;
    }
    String dalgonaImage = dalgonaImages.get(player.getUniqueId());
    NetworkRegistry
        network = Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel().sendTo(player, new PacketDalgonaRequest(dalgonaImage));
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
    setGameStatus(DalgonaGameStatus.IDLE);
  }

}
