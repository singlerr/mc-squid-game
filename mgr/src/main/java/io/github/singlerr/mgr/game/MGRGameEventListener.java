package io.github.singlerr.mgr.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.utils.EntitySerializable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RequiredArgsConstructor
public final class MGRGameEventListener implements GameEventListener {

  private final MGRGame game;
  private final MGRGameSetup gameSetup;
  private BossBar joiningTimeIndicator;

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
    long currentTime = System.currentTimeMillis();
    MGRGameContext gameContext = (MGRGameContext) context;
    log.info("현재 상태: {}", gameContext.getGameStatus());
    MGRGameSettings settings = (MGRGameSettings) gameContext.getSettings();
    gameContext.getSoundPlayer().tick();
    gameContext.getScheduler().tick();
    gameContext.getInterpolator().tick(currentTime);
    if (gameContext.getGameStatus() == MGRGameStatus.IDLE) {
      return;
    }

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
        if (e != null) {
          e.setGravity(false);
        }
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
            context.getSettings().setPillarLocation(e.getLocation());
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
              if (args[2].equalsIgnoreCase("q")) {
                context.endDoorSetup(player.getUniqueId());
                successCallback(sender, "설정 모드 해제");
                return;
              }
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
              if (args[2].equalsIgnoreCase("q")) {
                context.endDoorSetup(player.getUniqueId());
                successCallback(sender, "설정 모드 해제");
                return;
              }
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
      } else if (args[0].equalsIgnoreCase("close")) {
        game.getContext().closeSession();
        infoCallback(sender, "게임 초기화");
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
