package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Slf4j
public class RLGLGameEventListener implements GameEventListener {

  private BossBar timeIndicator;

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }
    if (timeIndicator != null) {
      timeIndicator.addPlayer(player.getPlayer());
    }

    RLGLGameContext ctx = (RLGLGameContext) context;
    RLGLGameSettings settings = ctx.getGameSettings();
    if (ctx.getYoungHee() != null) {
      PacketInitModel pkt =
          new PacketInitModel(settings.getYoungHee().getId(), ctx.getYoungHee().getEntityId(),
              settings.getFrontState(),
              settings.getModelLocation());
      NetworkRegistry network =
          Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
      network.getChannel().sendTo(player.getPlayer(), pkt);
    }

    player.getPlayer().setCustomNameVisible(true);
    context.syncName(player, context.getPlayers());
    context.syncName(context.getPlayers(), player);
  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        p.sendMessage(player.getUserDisplayName().append(Component.text(" 탈락").style(Style.style(
            NamedTextColor.RED))));
      } else {
        p.sendMessage(player.getAdminDisplayName().append(Component.text(" 탈락").style(Style.style(
            NamedTextColor.RED))));
      }
    }
    context.tryBanPlayer(player);
  }

  @Override
  public void onTick(GameContext context) {
    RLGLGameContext ctx = (RLGLGameContext) context;
    ctx.getSoundPlayer().tick();
    ctx.getScheduler().tick();
    if (ctx.getRlglStatus() == RLGLStatus.IDLE) {
      return;
    }

    long timePassed = System.currentTimeMillis() - ctx.getStartTime();
    RLGLGameSettings settings = (RLGLGameSettings) ctx.getSettings();
    long timeMillis = settings.getTime() * 1000L;
    if (timeIndicator != null) {
      long timeLeft = timeMillis - timePassed;
      timeIndicator.setProgress((double) timeLeft / (double) timeMillis);
      int totalSecs = (int) (timeLeft / 1000);
      int minute = totalSecs / 60;
      int seconds = totalSecs % 60;
      timeIndicator.setTitle("남은 시간: " + minute + "분 " + seconds + "초");
    }
    if (timePassed >= timeMillis) {
      ctx.end();
    }
  }

  @Override
  public void onStart(GameContext context) {
    RLGLGameContext ctx = (RLGLGameContext) context;
    RLGLGameSettings settings = (RLGLGameSettings) ctx.getSettings();
    ctx.setStartTime(System.currentTimeMillis());
    Entity younghee = settings.getYoungHee().toEntity();
    if (younghee != null) {
      younghee =
          younghee.getPassengers().stream().filter(e -> e instanceof Display).map(e -> (Display) e)
              .findAny().orElse(null);
      ctx.setYoungHee(younghee);
    }
    timeIndicator = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
    ctx.getPlayers().stream().filter(GamePlayer::available).map(GamePlayer::getPlayer)
        .forEach(p -> timeIndicator.addPlayer(p));
    ctx.executeForSpectators(timeIndicator::addPlayer);
  }

  @Override
  public void onEnd(GameContext context) {
    if (timeIndicator != null) {
      timeIndicator.hide();
      timeIndicator.removeAll();
      timeIndicator = null;
    }


    RLGLGameContext ctx = (RLGLGameContext) context;
    Collection<Player> adminPlayers = ctx.getOnlinePlayers(GameRole.ADMIN);
    for (GamePlayer player : ctx.getPlayers(GameRole.TROY.getLevel())) {
      if (!player.available()) {
        continue;
      }
      PlayerUtils.disableGlowing(player.getPlayer(), adminPlayers);
      player.getPlayer().setWalkSpeed(0.2f);
    }
  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {
    if (args.length < 1) {
      return;
    }
    String label = args[0];
    if (label.equalsIgnoreCase("tools")) {
      Player player = (Player) sender;
      for (RLGLItemRole role : RLGLItemRole.values()) {
        ItemStack stack = new ItemStack(Material.PAPER);
        player.getInventory().addItem(role.ofRole(stack));
      }
    }
  }
}
