package io.github.singlerr.sg.core.context;

import org.bukkit.command.CommandSender;

public interface GameEventBus {

  void postGameExit(GameContext context, GamePlayer player);

  void postGameJoin(GameContext context, GamePlayer player);

  void postGameTick(GameContext context);

  void postGameStart(GameContext context);

  void postGameEnd(GameContext context);

  void postSubCommand(CommandSender sender, String[] args);
}
