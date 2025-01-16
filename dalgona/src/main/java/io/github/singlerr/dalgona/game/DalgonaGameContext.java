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
import io.github.singlerr.sg.core.utils.PlayerUtils;
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
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DalgonaGameContext extends GameContext {

  private final Map<UUID, PlayerDalgonaStatus> playerStatusList;

  @Getter
  private final Map<UUID, Dalgona> providedDalgonaList;

  private final SecureRandom random;
  private final ChatColor glowingColor;
  @Getter
  @Setter
  private DalgonaGameStatus gameStatus;
  @Getter
  @Setter
  private long startTime;
  @Getter
  @Setter
  private int cutoff;


  public DalgonaGameContext(Map<UUID, GamePlayer> players,
                            GameStatus status,
                            GameEventBus eventBus,

                            GameSettings settings) {
    super(players, status, eventBus, settings);
    this.playerStatusList = new HashMap<>();
    this.providedDalgonaList = new HashMap<>();
    this.random = new SecureRandom();
    this.glowingColor = ChatColor.AQUA;
  }

  public DalgonaGameSettings getGameSettings() {
    return (DalgonaGameSettings) getSettings();
  }

  public List<GamePlayer> getPlayers(PlayerDalgonaStatus status) {
    return playerStatusList.entrySet().stream().filter(e -> {
      GamePlayer p = getPlayer(e.getKey());
      if (p == null) {
        return false;
      }
      if (!p.available()) {
        return false;
      }
      return e.getValue() == status;
    }).map(e -> getPlayer(e.getKey())).toList();
  }

  public void handleResult(Player player, PacketDalgonaResult packet) {
    if (gameStatus != DalgonaGameStatus.PROGRESS) {
      return;
    }

    GamePlayer p = getPlayer(player.getUniqueId());
    if (p == null) {
      return;
    }

    if (packet.isSuccess() &&
        playerStatusList.values().stream().filter(st -> st == PlayerDalgonaStatus.SUCCESS).count() <
            getCutoff()) {
      PlayerUtils.enableGlowing(player, getOnlinePlayers(GameRole.ADMIN), glowingColor);
    }

    playerStatusList.computeIfPresent(player.getUniqueId(),
        (id, t) -> packet.isSuccess() ? PlayerDalgonaStatus.SUCCESS : PlayerDalgonaStatus.FAILURE);
    if (!packet.isSuccess()) {
      broadcast(
          Component.text("플레이어 ").append(p.getAdminDisplayName()).append(Component.text(" 달고나 실패"))
              .style(
                  Style.style(NamedTextColor.RED)), GameRole.ADMIN);
    }
  }

  public PlayerDalgonaStatus getPlayerStatus(UUID id) {
    return playerStatusList.get(id);
  }

  public void startGame(int cutoff) {
    setCutoff(cutoff);
    setGameStatus(DalgonaGameStatus.PROGRESS);
    setStartTime(System.currentTimeMillis());
  }

  public void provideDalgona() {
    playerStatusList.clear();
    providedDalgonaList.clear();

    List<Dalgona> dalgonas = new ArrayList<>(getGameSettings().getDalgonaList().values());
    ItemStack dalgonaItem = new ItemStack(getGameSettings().getDalgonaType());
    ItemMeta meta = dalgonaItem.getItemMeta();
    meta.displayName(Component.text("달고나").style(Style.style(NamedTextColor.YELLOW)));
    meta.lore(
        List.of(Component.text("땅에 설치 후 우클릭").style(Style.style(NamedTextColor.YELLOW).decorate(
            TextDecoration.BOLD))));

    dalgonaItem.setItemMeta(meta);
    for (GamePlayer player : getPlayers()) {
      if (!player.available()) {
        continue;
      }
      if (player.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        Dalgona dalgona = dalgonas.get(random.nextInt(dalgonas.size()));
        providedDalgonaList.put(player.getPlayer().getUniqueId(), dalgona);
        playerStatusList.put(player.getId(), PlayerDalgonaStatus.IDLE);
        player.getPlayer().getInventory().addItem(dalgonaItem);
      }
    }
  }

  public void beginDalgona(Player player) {
    if (!providedDalgonaList.containsKey(player.getUniqueId())) {
      return;
    }
    Dalgona dalgona = providedDalgonaList.get(player.getUniqueId());
    NetworkRegistry
        network = Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
    network.getChannel()
        .sendTo(player, new PacketDalgonaRequest(dalgona.getImagePath(), dalgona.getThreshold()));
  }

  public void end() {
    List<GamePlayer> failedPlayers = playerStatusList.entrySet().stream()
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
