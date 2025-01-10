package io.github.singlerr.intermediary.game;

import io.github.singlerr.intermediary.Intermediary;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.Date;
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

    if (GameCore.getInstance().shouldBan()) {
      player.getPlayer().ban("오징어게임에서 탈락했습니다!", (Date) null, "", true);
    }
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
