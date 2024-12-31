package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.packets.PacketInitModel;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Slf4j
public class RLGLGameEventListener implements GameEventListener {

  private BossBar timeIndicator;

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (timeIndicator != null) {
      timeIndicator.addPlayer(player.getPlayer());
    }

    RLGLGameContext ctx = (RLGLGameContext) context;
    RLGLGameSettings settings = ctx.getGameSettings();
    if (ctx.getYoungHee() != null) {
      PacketInitModel pkt =
          new PacketInitModel(settings.getYoungHeeId(), ctx.getYoungHee().getEntityId(),
              settings.getFrontState(),
              settings.getModelLocation());
      NetworkRegistry network =
          Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
      network.getChannel().sendTo(player.getPlayer(), pkt);
    }
  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      p.getPlayer().sendMessage(Component.text(p.getPlayer().getDisplayName() + " 탈락"));
    }
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
    if (ctx.getYoungHee() == null && settings.getYoungHeeWorld() != null &&
        settings.getYoungHeeId() != null) {
      World world = Bukkit.getWorld(settings.getYoungHeeWorld());
      if (world != null) {
        Entity entity = world.getEntity(settings.getYoungHeeId());
        if (entity instanceof ArmorStand armorStand) {
          armorStand.customName(Component.text(armorStand.getUniqueId().toString()));
          armorStand.setCustomNameVisible(true);
          ctx.setYoungHee(armorStand);
          PacketInitModel pkt =
              new PacketInitModel(settings.getYoungHeeId(), armorStand.getEntityId(),
                  settings.getFrontState(),
                  settings.getModelLocation());
          NetworkRegistry network =
              Bukkit.getServicesManager().getRegistration(NetworkRegistry.class).getProvider();
          network.getChannel().sendToAll(pkt);
        }
      }
    }
    timeIndicator = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
    ctx.getPlayers().stream().map(GamePlayer::getPlayer).forEach(p -> timeIndicator.addPlayer(p));
  }

  @Override
  public void onEnd(GameContext context) {
    if (timeIndicator != null) {
      timeIndicator.hide();
      timeIndicator.removeAll();
      timeIndicator = null;
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
