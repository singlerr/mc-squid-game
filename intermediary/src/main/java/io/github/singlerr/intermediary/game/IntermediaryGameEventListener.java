package io.github.singlerr.intermediary.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;

@Slf4j
public final class IntermediaryGameEventListener implements GameEventListener {

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (!player.available()) {
      return;
    }
    player.getPlayer().setCustomNameVisible(true);
    if (player.getRole().getLevel() <= GameRole.TROY.getLevel()) {
      context.assignNumberName(player);
      context.syncName(player, GameRole.ADMIN);
      PlayerUtils.changeSkin(player.getPlayer(), GameRole.USER);
    } else {
      player.setUserDisplayName(Component.text("?"));
    }
    context.syncNameLowerThan(GameRole.USER.getLevel(), player);
    context.syncNameLowerThan(player, GameRole.USER.getLevel());
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
