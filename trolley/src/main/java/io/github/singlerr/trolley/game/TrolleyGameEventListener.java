package io.github.singlerr.trolley.game;

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
import org.slf4j.helpers.MessageFormatter;

@RequiredArgsConstructor
public final class TrolleyGameEventListener implements GameEventListener {

  private final TrolleyGame game;

  @Override
  public void onJoin(GameContext context, GamePlayer player) {

  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {
    for (GamePlayer p : context.getPlayers()) {
      if (p.getRole() == GameRole.USER) {
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
    TrolleyGameContext ctx = (TrolleyGameContext) context;
    ctx.getScheduler().tick();
    ctx.getSoundPlayer().tick();
    if (ctx.getGameStatus() != TrolleyGameStatus.PROGRESS) {
      return;
    }

    long time = System.currentTimeMillis();
    ctx.tickTrains(time);
    ctx.tickPlayers(time);
  }

  @Override
  public void onStart(GameContext context) {
    TrolleyGameContext ctx = (TrolleyGameContext) context;
    ctx.loadTrainEntities();
    ctx.setGameStatus(TrolleyGameStatus.PROGRESS);
  }

  @Override
  public void onEnd(GameContext context) {

  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {
    if (args.length > 1) {
      Player p = (Player) sender;
      if (args[0].equalsIgnoreCase("fire")) {
        try {
          int trackNum = Integer.parseInt(args[1]);
          if (game.getContext().getTrainEntity(trackNum) == null) {
            errorCallback(sender, "해당 선로의 열차 엔티티가 존재하지 않습니다.");
            return;
          }
          if (game.getContext().fireTrain(trackNum)) {
            successCallback(sender, "{}번 선로 열차 발진 시작", trackNum);
          } else {
            errorCallback(sender, "이미 해당 선로에 열차가 발진중입니다.");
          }
        } catch (NumberFormatException e) {
          errorCallback(sender, "자연수를 입력하세요.");
        }
      } else if (args[0].equalsIgnoreCase("track")) {
        try {
          int trackNum = Integer.parseInt(args[1]);
          game.getSetup().getContext().beginTrack(p.getUniqueId(), trackNum);
          successCallback(sender, "이제 나무 도끼를 이용해 선로 시작/끝 지점을 선택하거나, 엔티티를 우클릭하여 열차 엔티티를 지정하세요.");
        } catch (NumberFormatException e) {
          if (args[1].equalsIgnoreCase("q")) {
            game.getSetup().getContext().endTrack(p.getUniqueId());
            successCallback(sender, "설정 모드 해제");
            return;
          }
          errorCallback(sender, "자연수를 입력하세요.");
        }
      } else if (args[0].equalsIgnoreCase("region")) {
        if (game.getSetup().getContext().getGameRegionBuilder(p.getUniqueId()) != null) {
          game.getSetup().getContext().endGameRegion(p.getUniqueId());
          successCallback(sender, "설정 모드 해제");
        } else {
          game.getSetup().getContext().beginGameRegion(p.getUniqueId());
          successCallback(sender, "이제 나무 도끼를 이용해 게임 지역을 선택하세요.");
        }
      } else if (args[0].equalsIgnoreCase("start")) {
        game.getContext().startIntermissions();
        successCallback(sender, "게임 시작");
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
