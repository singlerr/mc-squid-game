package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import io.github.singlerr.sg.core.utils.Interpolator;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.slf4j.helpers.MessageFormatter;

@RequiredArgsConstructor
public final class MGRGameEventListener implements GameEventListener {

  private final MGRGame game;
  private final MGRGameSetup gameSetup;
  private BossBar joiningTimeIndicator;

  @Override
  public void onJoin(GameContext context, GamePlayer player) {
    if (player.getRole() == GameRole.USER) {
      context.syncName(player, GameRole.ADMIN);
    } else {
      context.syncName(GameRole.USER, player);
    }
  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole() == GameRole.USER) {
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
    MGRGameContext gameContext = (MGRGameContext) context;
    MGRGameSettings settings = (MGRGameSettings) gameContext.getSettings();
    gameContext.getSoundPlayer().tick();
    gameContext.getScheduler().tick();
    Interpolator interpolator = gameContext.getInterpolator();
    if (interpolator != null) {
      if (interpolator.end()) {
        gameContext.setInterpolator(null);
      } else {
        interpolator.tick();
      }
    }
    if (gameContext.getGameStatus() == MGRGameStatus.IDLE) {
      return;
    }

    long currentTime = System.currentTimeMillis();
    if (gameContext.getGameStatus() == MGRGameStatus.JOINING_ROOM) {
      long timePassed = currentTime - gameContext.getJoiningStartedTime();
      long joiningRoomTime = (long) (settings.getJoiningRoomTime() * 1000L);
      long timeRemaining = joiningRoomTime - timePassed;

      if (timeRemaining > 0) {
        if (joiningTimeIndicator == null) {
          joiningTimeIndicator = Bukkit.createBossBar("", BarColor.RED, BarStyle.SOLID);
        }
        int secs = (int) (timeRemaining / 1000L);
        int minute = secs / 60;
        int seconds = secs % 60;
        joiningTimeIndicator.setTitle("남은 시간: " + minute + "분 " + seconds + "초");
        joiningTimeIndicator.setProgress((float) timeRemaining / (float) joiningRoomTime);
      } else {
        gameContext.startClosingRoom();
      }
    }
  }

  @Override
  public void onStart(GameContext context) {
    MGRGameSettings settings = ((MGRGameContext) context).getGameSettings();
    if (settings.getPillarEntity() != null) {
      World w = Bukkit.getWorld(settings.getPillarEntity().getWorld());
      if (w != null) {
        Entity e = w.getEntity(settings.getPillarEntity().getId());
        ((MGRGameContext) context).setPillar(e);
      }
    }
  }

  @Override
  public void onEnd(GameContext context) {

  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {
    if (args.length >= 2) {
      if (args[0].equalsIgnoreCase("setup")) {
        String type = args[1];
        Player player = (Player) sender;
        MGRGameSetupContext context = gameSetup.getContext();
        if (type.equalsIgnoreCase("pillar")) {
          context.beginPillarSetup(player.getUniqueId(), (e) -> {
            context.getSettings()
                .setPillarEntity(new EntitySerializable(e.getWorld().getName(), e.getUniqueId()));
          });
          infoCallback(sender, "이제 블레이즈 막대를 이용해 엔티티를 지정하세요.");
        } else if (type.equalsIgnoreCase("door")) {
          if (args.length > 2) {
            try {
              int doorNum = Integer.parseInt(args[2]);
              context.beginDoorSetup(player.getUniqueId(), (loc) -> {
                context.getSettings().getDoors().put(doorNum, loc);
              });
              infoCallback(sender, "이제 막대기를 이용해 블럭을 지정하세요.");
            } catch (NumberFormatException e) {
              errorCallback(sender, "자연수를 입력하세요.");
            }
          }
        } else if (type.equalsIgnoreCase("room")) {
          if (args.length > 2) {
            try {
              int roomNum = Integer.parseInt(args[2]);
              context.beginSelector(player.getUniqueId(), roomNum);
              infoCallback(sender, "이제 나무 도끼를 이용해 영역을 지정하세요.");
            } catch (NumberFormatException e) {
              errorCallback(sender, "자연수를 입력하세요.");
            }
          }
        }
      } else if (args[0].equalsIgnoreCase("invoke")) {
        String count = args[1];
        try {
          int playerCount = Integer.parseInt(count);
          game.getContext().startNewSession(playerCount);
          infoCallback(sender, "게임 시작 - 정원: {}명", playerCount);
        } catch (NumberFormatException e) {
          errorCallback(sender, "자연수를 입력하세요.");
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
