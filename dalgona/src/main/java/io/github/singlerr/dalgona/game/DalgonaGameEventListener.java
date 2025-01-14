package io.github.singlerr.dalgona.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.PlayerUtils;
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
    if (!player.available()) {
      return;
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
        p
            .sendMessage(
                player.getAdminDisplayName().append(Component.text(" 탈락").style(Style.style(
                    NamedTextColor.RED))));
      }
    }

    context.tryBanPlayer(player);
  }

  @Override
  public void onTick(GameContext context) {
    DalgonaGameContext ctx = (DalgonaGameContext) context;
    if (ctx.getGameStatus() != DalgonaGameStatus.PROGRESS) {
      return;
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

      ctx.end();
    }

    if (timeIndicator != null) {
      int totalSecs = (int) (timeLeft / 1000);
      int minutes = totalSecs / 60;
      int seconds = totalSecs % 60;

      int size = ctx.getPlayers(PlayerDalgonaStatus.SUCCESS).size();
      String color = size > ctx.getCutoff() ? "§c" : "§f";
      timeIndicator.setProgress((double) timeLeft / (double) timeLimit);
      timeIndicator.setTitle(
          MessageFormatter.basicArrayFormat("남은 시간: {}분 {}초, 선착순 " + color + "{}§f/{} 명",
              new Object[] {minutes, seconds, ctx.getPlayers(PlayerDalgonaStatus.SUCCESS).size(),
                  ctx.getCutoff()}));
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

    PlayerUtils.disableGlowing(ctx.getOnlinePlayers(GameRole.TROY.getLevel()),
        ctx.getOnlinePlayers(GameRole.ADMIN));
  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {
    DalgonaGameContext context = game.getContext();
    if (args.length > 0) {
      if (args[0].equalsIgnoreCase("provide")) {
        context.provideDalgona();
        successCallback(sender, "지급 완료");
      } else if (args[0].equalsIgnoreCase("start")) {
        if (args.length < 2) {
          errorCallback(sender, "선착순 인원을 입력하세요.");
          return;
        }
        if (context.getGameStatus() == DalgonaGameStatus.IDLE) {
          errorCallback(sender, "이미 게임이 진행중입니다!");
        } else {
          int cutoff;
          try {
            cutoff = Integer.parseInt(args[1]);
          } catch (NumberFormatException e) {
            errorCallback(sender, "숫자를 입력하세요.");
            return;
          }

          context.startGame(cutoff);
          successCallback(sender, "게임이 시작되었습니다. 선착순 {}명", cutoff);
          if (timeIndicator != null) {
            timeIndicator.removeAll();
            timeIndicator.hide();
          }
          timeIndicator = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
          for (GamePlayer player : context.getPlayers()) {
            if (player.available()) {
              timeIndicator.addPlayer(player.getPlayer());
            }
          }

          context.executeForSpectators(timeIndicator::addPlayer);

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
      } else if (args[0].equalsIgnoreCase("cutoff")) {
        if (args.length < 2) {
          errorCallback(sender, "선착순 인원을 입력하세요.");
          return;
        }

        int cutoff;
        try {
          cutoff = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
          errorCallback(sender, "숫자를 입력하세요.");
          return;
        }

        successCallback(sender, "선착순 {}명으로 설정 완료", cutoff);
        game.getContext().setCutoff(cutoff);
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
