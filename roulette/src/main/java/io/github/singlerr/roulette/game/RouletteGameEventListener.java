package io.github.singlerr.roulette.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.helpers.MessageFormatter;

@RequiredArgsConstructor
public final class RouletteGameEventListener implements GameEventListener {

  private final RouletteGame game;

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (player.getRole().getLevel() <= GameRole.TROY.getLevel()) {
      context.syncName(player, GameRole.ADMIN);
    } else {
      context.syncNameLowerThan(GameRole.TROY.getLevel(), player);
    }
  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        p.getPlayer()
            .sendMessage(p.getUserDisplayName().append(Component.text(" 탈락").style(Style.style(
                NamedTextColor.RED))));
      } else {
        p.getPlayer()
            .sendMessage(p.getAdminDisplayName().append(Component.text(" 탈락").style(Style.style(
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
    if (args.length > 1) {
      Player player = (Player) sender;
      try {
        int bulletAmount = Integer.parseInt(args[0]);
        int realBulletAmount = Integer.parseInt(args[1]);
        if (realBulletAmount > bulletAmount) {
          errorCallback(sender, "전체 탄환의 수는 진탄환의 수보다 작을 수 없습니다.");
          return;
        }

        Gun gun = game.getContext().createGun(bulletAmount, realBulletAmount);
        ItemStack gunItem = game.getContext().createGun(gun);
        player.getInventory().addItem(gunItem);

        successCallback(sender, "리볼버가 지급되었습니다. 쉬프트 - 우클릭으로 리볼버 재장전이 가능합니다.");
      } catch (NumberFormatException e) {
        errorCallback(sender, "자연수를 입력하세요.");
      }
    }
  }

  private boolean errorCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)).style(
        Style.style(NamedTextColor.RED)));
    return false;
  }

  private boolean successCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)).style(
        Style.style(NamedTextColor.GREEN)));
    return false;
  }

  private boolean infoCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)).style(
        Style.style(NamedTextColor.GREEN)));
    return false;
  }
}
