package io.github.singlerr.sg.core.commands;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.GameStorage;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.Gender;
import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.io.IOException;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.helpers.MessageFormatter;

public final class GameCommands implements CommandExecutor, TabCompleter {

  private final GameLifecycle gameLifecycle;
  private final GameRegistry games;
  private final GameSetupManager setupManager;
  private final GameStorage storage;

  public GameCommands(GameLifecycle lifecycle, GameRegistry games, GameSetupManager setupManager,
                      GameStorage storage) {
    this.gameLifecycle = lifecycle;
    this.games = games;
    this.setupManager = setupManager;
    this.storage = storage;
  }

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (args.length < 1) {
      return false;
    }

    String subCmd = args[0];
    if (subCmd.equalsIgnoreCase("mod")) {
      if (args.length < 2) {
        return errorCallback(sender, "게임 ID를 입력하세요.");
      }
      String id = args[1];
      Game game = this.games.getById(id);
      if (game == null) {
        return errorCallback(sender, "{} 게임은 존재하지 않습니다.", id);
      }

      String[] slicedArgs = slice(args, 2);
      modCommand(id, game, sender, slicedArgs);
      return true;
    }

    if (subCmd.equalsIgnoreCase("list")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      gameListCommand(sender, args);
      return true;
    } else if (subCmd.equalsIgnoreCase("save")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      try {
        if (!GameCore.getInstance().getDataFolder().exists()) {
          GameCore.getInstance().getDataFolder().mkdir();
        }
        storage.save();
        successCallback(sender, "저장 완료");
      } catch (IOException ex) {
        errorCallback(sender, "설정 저장에 실패했습니다");
      }
    } else if (subCmd.equalsIgnoreCase("reload")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      try {
        storage.loadSettings();
      } catch (IOException e) {
        errorCallback(sender, "리로드에 실패했습니다. games.json 파일이 있는지 확인하세요.");
      }
    } else if (subCmd.equalsIgnoreCase("banmode")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      if (GameCore.getInstance().shouldBan()) {
        infoCallback(sender, "탈락시 밴 비활성화");
        GameCore.getInstance().shouldBan(false);
      } else {
        infoCallback(sender, "탈락시 밴 활성화");
        GameCore.getInstance().shouldBan(true);
      }
    } else if (subCmd.equalsIgnoreCase("loadskins")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      Bukkit.getScheduler().runTaskAsynchronously(GameCore.getInstance(), () -> {
        infoCallback(sender, "관리자 스킨 로드 중...");
        PlayerUtils.loadSkin(GameCore.getInstance().getAdminSkinUrl(), false);
        infoCallback(sender, "여성 스킨 로드 중...");
        for (String url : GameCore.getInstance().getPlayerSkinUrl(false)) {
          PlayerUtils.loadSkin(url, true);
        }
        infoCallback(sender, "남성 스킨 로드 중...");
        for (String url : GameCore.getInstance().getPlayerSkinUrl(true)) {
          PlayerUtils.loadSkin(url, false);
        }

        infoCallback(sender, "스킨 로드 완료");
      });
    } else if (subCmd.equalsIgnoreCase("syncnames")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return false;
      }
      GameLifecycle.GameInfo info = gameLifecycle.getCurrentGame();
      if (info == null) {
        errorCallback(sender, "게임이 진행중이지 않습니다.");
        return false;
      }

      GameContext context = info.context();
      if (context == null) {
        errorCallback(sender, "게임이 진행중이지 않습니다.");
        return false;
      }

      Bukkit.getScheduler().runTaskAsynchronously(GameCore.getInstance(), () -> {
        for (GamePlayer player : context.getPlayers()) {
          context.syncName(player, context.getPlayers());
          context.syncName(context.getPlayers(), player);
        }

        successCallback(sender, "닉네임 재동기화 완료");
      });
    }
    return true;
  }

  private boolean errorCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(
        Component.text(MessageFormatter.basicArrayFormat(message, args)).style(Style.style(
            NamedTextColor.RED)));
    return false;
  }

  private boolean successCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(
        Component.text(MessageFormatter.basicArrayFormat(message, args)).style(Style.style(
            NamedTextColor.GREEN)));
    return false;
  }

  private boolean infoCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(
        Component.text(MessageFormatter.basicArrayFormat(message, args)).style(Style.style(
            NamedTextColor.BLUE)));
    return false;
  }

  private void gameListCommand(CommandSender sender, String[] args) {
    infoCallback(sender, "게임 목록: {}", games.keys());
  }

  private void modCommand(String id, Game game, CommandSender sender, String[] args) {
    String label = args[0];
    String[] slicedArgs = slice(args, 1);
    if (label.equalsIgnoreCase("setup")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return;
      }
      setupCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("exec")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return;
      }
      execCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("join")) {
      joinCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("exit")) {
      exitCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("start")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return;
      }
      startCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("stop")) {
      if (!sender.isOp()) {
        errorCallback(sender, "권한이 없습니다.");
        return;
      }
      stopCommand(id, game, sender, slicedArgs);
    }
  }

  private void startCommand(String id, Game game, CommandSender sender, String[] args) {
    if (gameLifecycle.getCurrentGame() != null) {
      errorCallback(sender, "현재 {} 게임이 진행중입니다. 먼저 이 게임을 종료하고 다시 시도하세요.",
          gameLifecycle.getCurrentGame().id());
      return;
    }
    gameLifecycle.startGame(id, () -> {
      successCallback(sender, "게임 시작: {}", id);
    });
  }

  private void stopCommand(String id, Game game, CommandSender sender, String[] args) {
    if (gameLifecycle.getCurrentGame() == null) {
      errorCallback(sender, "현재 진행중인 게임이 없습니다.");
      return;
    }

    if (!gameLifecycle.getCurrentGame().id().equals(id)) {
      errorCallback(sender, "게임 {}(은)는 진행중이 아닙니다. 현재 진행중인 게임: {}", id,
          gameLifecycle.getCurrentGame().id());
      return;
    }

    gameLifecycle.endGame(() -> {
      successCallback(sender, "{} 게임이 종료되었습니다.", id);
    });
  }


  private void joinCommand(String id, Game game, CommandSender sender, String[] args) {
    if (args.length < 2) {
      errorCallback(sender, "게임 계급과 성별을 입력하세요.");
      return;
    }

    String rawRole = args[0].toUpperCase();
    GameRole role = EnumUtils.getEnum(GameRole.class, rawRole);
    Gender gender = EnumUtils.getEnum(Gender.class, args[1].toUpperCase());

    Player player;
    if (args.length > 3) {
      String targetPlayerName = args[2];
      Player p = Bukkit.getPlayer(targetPlayerName);
      if (p == null) {
        errorCallback(sender, "해당 플레이어는 존재하지 않습니다.");
        return;
      }

      player = p;
    } else {
      player = (Player) sender;
    }

    if (role == null) {
      errorCallback(sender, "알맞은 계급을 선택하세요: {}", new Object[] {GameRole.values()});
      return;
    }

    if (gender == null) {
      errorCallback(sender, "성별을 선택하세요: male/female");
      return;
    }

    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "현재 게임이 진행중이지 않습니다.");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "현재 진행중인 게임에 참가해야합니다. [현재 진행중 : {}, 입력값 : {}]", gameInfo.id(),
          id);
      return;
    }

    gameInfo.context().joinPlayer(new GamePlayer(player, role, gender));
    infoCallback(player, "게임에 참가했습니다!");
  }

  private void exitCommand(String id, Game game, CommandSender sender, String[] args) {
    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "게임이 진행중이지 않습니다.");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "현재 진행중인 게임에 참가해야합니다. [현재 진행중 : {}, 입력값 : {}]", gameInfo.id(),
          id);
      return;
    }
    Player player = (Player) sender;
    GamePlayer gamePlayer = gameInfo.context().getPlayer(player.getUniqueId());
    if (gamePlayer == null) {
      errorCallback(sender, "게임에 참가해있지 않습니다.");
      return;
    }

    gameInfo.context().kickPlayer(gamePlayer);
    infoCallback(sender, "게임에서 퇴장했습니다.");
  }

  private void execCommand(String id, Game game, CommandSender sender, String[] args) {
    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "현재 게임이 진행중이지 않습니다.");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "현재 진행중인 게임에 참가해야합니다. [현재 진행중 : {}, 입력값 : {}]", gameInfo.id(),
          id);
      return;
    }

    gameInfo.eventBus().postSubCommand(sender, args);
  }

  private void setupCommand(String id, Game game, CommandSender sender, String[] args) {
    if (args.length < 1) {
      errorCallback(sender, "Specify an action : on/off");
      return;
    }
    Player player = (Player) sender;
    String action = args[0];
    if (action.equalsIgnoreCase("on")) {
      if (!setupManager.joinSetup(player.getUniqueId(), id)) {
        errorCallback(sender, "You are already on setup!");
      } else {
        successCallback(sender, "You are now on setup");
      }

    } else if (action.equalsIgnoreCase("off")) {
      if (!setupManager.exitSetup(player.getUniqueId())) {
        errorCallback(sender, "You are not now on setup!");
      } else {
        successCallback(sender, "You are not now on setup");
      }
    }
  }

  private String[] slice(String[] args, int startIndex) {
    String[] slice = new String[args.length - startIndex];
    for (int i = startIndex, destIdx = 0; i < args.length; i++, destIdx++) {
      slice[destIdx] = args[i];
    }

    return slice;
  }

  @Override
  public @Nullable List<String> onTabComplete(@NotNull CommandSender sender,
                                              @NotNull Command command, @NotNull String label,
                                              @NotNull String[] args) {
    return List.of();
  }
}
