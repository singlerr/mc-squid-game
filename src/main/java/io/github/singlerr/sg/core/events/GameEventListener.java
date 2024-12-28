package io.github.singlerr.sg.core.events;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import org.bukkit.command.CommandSender;

public interface GameEventListener {

  void onJoin(GameContext context, GamePlayer player);

  void onExit(GameContext context, GamePlayer player);

  void onTick(GameContext context);

  void onStart(GameContext context);

  void onEnd(GameContext context);

  void onSubCommand(CommandSender sender, String[] args);
}
