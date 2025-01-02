package io.github.singlerr.intermediary.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;

public final class IntermediaryGameEventListener implements GameEventListener {

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }
    context.assignNumberName(player);
    if (player.getRole().getLevel() <= GameRole.TROY.getLevel()) {
      context.syncName(player, GameRole.ADMIN);
      PlayerUtils.changeSkin(player.getPlayer(), GameRole.USER);
    } else {
      context.syncNameLowerThan(GameRole.USER.getLevel(), player);
    }
  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        p.sendMessage(p.getUserDisplayName().append(Component.text(" 탈락").style(Style.style(
            NamedTextColor.RED))));
      } else {
        p.sendMessage(p.getAdminDisplayName().append(Component.text(" 탈락").style(Style.style(
            NamedTextColor.RED))));
      }
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
