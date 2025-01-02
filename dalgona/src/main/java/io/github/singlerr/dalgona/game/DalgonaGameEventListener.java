package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.slf4j.helpers.MessageFormatter;

public final class DalgonaGameEventListener implements GameEventListener {

  private final DalgonaGame game;
  private BossBar timeIndicator;

  public DalgonaGameEventListener(DalgonaGame game) {
    this.game = game;
  }

  @Override
  public void onJoin(GameContext context, GamePlayer player) {

  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole().getLevel() <= GameRole.TROY.getLevel()) {
        p.sendMessage(p.getUserDisplayName().append(Component.text(" 탈락").style(Style.style(
            NamedTextColor.RED))));
      } else {
        p
            .sendMessage(p.getAdminDisplayName().append(Component.text(" 탈락").style(Style.style(
                NamedTextColor.RED))));
      }
    }
  }

  @Override
  public void onTick(GameContext context) {
    DalgonaGameContext ctx = (DalgonaGameContext) context;
    if (ctx.getGameStatus() != DalgonaGameStatus.PROGRESS) {
      return;
    }
    if (timeIndicator == null) {
      timeIndicator = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
    }

    long currentTime = System.currentTimeMillis();
    long timePassed = currentTime - ctx.getStartTime();
    long timeLimit = (long) (ctx.getGameSettings().getTime() * 1000);
    long timeLeft = timeLimit - timePassed;
    if (timeLeft <= 0) {
      if (timeIndicator != null) {
        timeIndicator.hide();
        timeIndicator.removeAll();
        timeIndicator = null;
      }
    }
    if (timeIndicator != null) {
      int totalSecs = (int) (timeLeft / 1000);
      int minutes = totalSecs / 60;
      int seconds = totalSecs % 60;

      timeIndicator.setProgress((double) timeLeft / (double) timeLimit);
      timeIndicator.setTitle("남은 시간: " + minutes + "분" + seconds + "초");
    }
  }

  @Override
  public void onStart(GameContext context) {

  }

  @Override
  public void onEnd(GameContext context) {
    DalgonaGameContext ctx = (DalgonaGameContext) context;
    ctx.setGameStatus(DalgonaGameStatus.IDLE);
    if (timeIndicator != null) {
      timeIndicator.hide();
      timeIndicator.removeAll();
      timeIndicator = null;
    }

  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {
    DalgonaGameContext context = game.getContext();
    if (args.length > 0) {
      if (args[0].equalsIgnoreCase("provide")) {
        context.provideDalgona();
        successCallback(sender, "지급 완료");
      } else if (args[0].equalsIgnoreCase("start")) {
        if (context.getGameStatus() == DalgonaGameStatus.IDLE) {
          errorCallback(sender, "이미 게임이 진행중입니다!");
        } else {
          context.startGame();
          successCallback(sender, "게임이 시작되었습니다.");
        }
      } else if (args[0].equalsIgnoreCase("pause")) {
        if (context.getGameStatus() == DalgonaGameStatus.PROGRESS) {
          context.setGameStatus(DalgonaGameStatus.IDLE);
          successCallback(sender, "게임이 중지되었습니다.");
        } else {
          errorCallback(sender, "게임이 진행중이 아닙니다!");
        }
      } else if (args[0].equalsIgnoreCase("resume")) {
        if (context.getGameStatus() == DalgonaGameStatus.IDLE) {
          context.setGameStatus(DalgonaGameStatus.PROGRESS);
          successCallback(sender, "게임이 재개되었습니다.");
        } else {
          errorCallback(sender, "게임이 정지중이 아닙니다!");
        }
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
