package io.github.singlerr.intermediary.game;

import io.github.singlerr.intermediary.Intermediary;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

@Slf4j
public final class IntermediaryGameEventListener implements GameEventListener {

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }

    Bukkit.getScheduler().runTaskAsynchronously(Intermediary.getInstance(), () -> {
      player.getPlayer().setCustomNameVisible(true);

      boolean isUser = player.getRole().getLevel() <= GameRole.TROY.getLevel();
      if (isUser) {
        PlayerUtils.changeSkin(player.getPlayer(), GameRole.USER, player.getGender());
      }
      Bukkit.getScheduler().scheduleSyncDelayedTask(Intermediary.getInstance(), () -> {
        player.available();

        if (isUser) {
          context.assignNumberName(player);
          player.sendMessage(
              Component.text("당신의 번호는 ").style(Style.style(NamedTextColor.YELLOW)).append(
                  Component.text(player.getUserNumber()).style(Style.style(NamedTextColor.AQUA))
                      .append(Component.text("번 입니다.").style(Style.style(NamedTextColor.YELLOW)))));
        } else {
          player.setUserDisplayName(Component.text("?"));
        }
        context.syncName(player, context.getPlayers());
        context.syncName(context.getPlayers(), player);
      }, 20L);
    });

  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }
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

  }

  @Override
  public void onStart(GameContext context) {

  }

  @Override
  public void onEnd(GameContext context) {

  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {

  }
}
