package io.github.singlerr.sg.core.commands;

import io.github.singlerr.sg.core.Game;
import io.github.singlerr.sg.core.GameLifecycle;
import io.github.singlerr.sg.core.GameRegistry;
import io.github.singlerr.sg.core.GameSetupManager;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.EnumUtils;
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

  public GameCommands(GameLifecycle lifecycle, GameRegistry games, GameSetupManager setupManager) {
    this.gameLifecycle = lifecycle;
    this.games = games;
    this.setupManager = setupManager;
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
        return errorCallback(sender, "Specify game id");
      }
      String id = args[1];
      Game game = this.games.getById(id);
      if (game == null) {
        return errorCallback(sender, "Game with id {} does not exist", id);
      }

      String[] slicedArgs = slice(args, 2);
      modCommand(id, game, sender, slicedArgs);
      return true;
    }

    if (subCmd.equalsIgnoreCase("list")) {
      gameListCommand(sender, args);
      return true;
    }
    return true;
  }

  private boolean errorCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }

  private boolean successCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }

  private boolean infoCallback(CommandSender sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }

  private void gameListCommand(CommandSender sender, String[] args) {
    infoCallback(sender, "Games: {}", games.keys());
  }

  private void modCommand(String id, Game game, CommandSender sender, String[] args) {
    String label = args[0];
    if (label.equalsIgnoreCase("setup")) {
      String[] slicedArgs = slice(args, 1);
      setupCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("exec")) {
      String[] slicedArgs = slice(args, 1);
      execCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("join")) {
      String[] slicedArgs = slice(args, 1);
      joinCommand(id, game, sender, slicedArgs);
    } else if (label.equalsIgnoreCase("exit")) {
      String[] slicedArgs = slice(args, 1);
      exitCommand(id, game, sender, slicedArgs);
    }
  }

  private void joinCommand(String id, Game game, CommandSender sender, String[] args) {
    if (args.length < 1) {
      errorCallback(sender, "Specify a role : ", (Object) GameRole.values());
      return;
    }
    String rawRole = args[0];
    GameRole role = EnumUtils.getEnum(GameRole.class, rawRole);
    if (role == null) {
      errorCallback(sender, "Game role named '{}' does not exist", rawRole);
      return;
    }

    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "Currently no games are playing");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "Game mismatch; Currently playing: {}, your input: {}", gameInfo.id(),
          id);
      return;
    }

    gameInfo.context().joinPlayer(new GamePlayer((Player) sender, role));
  }

  private void exitCommand(String id, Game game, CommandSender sender, String[] args) {
    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "Currently no games are playing");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "Game mismatch; Currently playing: {}, your input: {}", gameInfo.id(),
          id);
      return;
    }
    Player player = (Player) sender;
    GamePlayer gamePlayer = gameInfo.context().getPlayer(player.getUniqueId());
    if (gamePlayer == null) {
      errorCallback(sender, "Player with {} was not in the game {}", player.getName(), id);
      return;
    }

    gameInfo.context().kickPlayer(gamePlayer);
  }

  private void execCommand(String id, Game game, CommandSender sender, String[] args) {
    GameLifecycle.GameInfo gameInfo = gameLifecycle.getCurrentGame();

    if (gameInfo == null) {
      errorCallback(sender, "Currently no games are playing");
      return;
    }

    if (!gameInfo.id().equals(id)) {
      errorCallback(sender, "Game mismatch; Currently playing: {}, your input: {}", gameInfo.id(),
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
      }
    } else {
      successCallback(sender, "You are not now on setup");
      return;
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
