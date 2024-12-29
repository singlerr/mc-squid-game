package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.events.GameEventListener;
import java.util.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class RLGLGameEventListener implements GameEventListener {

  @Override
  public void onJoin(GameContext context, GamePlayer player) {

  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {

  }

  @Override
  public void onTick(GameContext context) {
    RLGLGameContext ctx = (RLGLGameContext) context;
    ctx.getSoundPlayer().tick();
    if (ctx.getRlglStatus() != RLGLStatus.IDLE) {
      return;
    }

    if (ctx.getRotationAnimator() != null) {
      if (ctx.getRotationAnimator().end()) {
        ctx.setRotationAnimator(null);
      } else {
        ctx.getRotationAnimator().tick();
      }
    }

    int timePassed =
        (int) TimeUnit.SECONDS.convert(System.currentTimeMillis() - ctx.getStartTime(),
            TimeUnit.MILLISECONDS);

    RLGLGameSettings settings = (RLGLGameSettings) ctx.getSettings();
    if (timePassed >= settings.getTime()) {
      ctx.end();
    }
  }

  @Override
  public void onStart(GameContext context) {
    RLGLGameContext ctx = (RLGLGameContext) context;
    RLGLGameSettings settings = (RLGLGameSettings) ctx.getSettings();
    if (settings.getYoungHeeWorld() != null && settings.getYoungHeeId() != null) {
      World world = Bukkit.getWorld(settings.getYoungHeeWorld());
      if (world != null) {
        Entity entity = world.getEntity(settings.getYoungHeeId());
        if (entity instanceof ArmorStand armorStand) {
          ctx.setYoungHee(armorStand);
        }
      }
    }
  }

  @Override
  public void onEnd(GameContext context) {

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
