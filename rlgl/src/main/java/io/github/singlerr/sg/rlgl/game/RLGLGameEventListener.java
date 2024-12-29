package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.events.GameEventListener;
import java.util.concurrent.TimeUnit;
import org.bukkit.command.CommandSender;

public class RLGLGameEventListener implements GameEventListener {
  @Override
  public void onJoin(GameContext context, GamePlayer player) {

  }

  @Override
  public void onExit(GameContext context, GamePlayer player) {

  }

  @Override
  public void onTick(GameContext context) {
    RLGLGameContext ctx = (RLGLGameContext) context;
    ctx.getSoundPlayer().tick();
    if (ctx.getRlglStatus() != RLGLStatus.IDLE) {
      return;
    }

    int timePassed =
        (int) TimeUnit.SECONDS.convert(System.currentTimeMillis() - ctx.getStartTime(),
            TimeUnit.MILLISECONDS);

    RLGLGameSettings settings = (RLGLGameSettings) ctx.getSettings();
    if (timePassed >= settings.getTime()) {
      ctx.end();
    }
  }

  @Override
  public void onStart(GameContext context) {

  }

  @Override
  public void onEnd(GameContext context) {

  }

  @Override
  public void onSubCommand(CommandSender sender, String[] args) {

  }
}
