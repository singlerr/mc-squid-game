package io.github.singlerr.sg.core.context.impl;

import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.events.GameEventListener;
import io.github.singlerr.sg.core.registry.Registry;
import org.bukkit.command.CommandSender;

public class DefaultGameEventBus implements GameEventBus {

  private final Registry<GameEventListener> listeners;

  public DefaultGameEventBus(Registry<GameEventListener> listeners) {
    this.listeners = listeners;
  }

  @Override
  public void postGameExit(GameContext context, GamePlayer player) {
    listeners.values().forEach(l -> l.onExit(context, player));
  }

  @Override
  public void postGameJoin(GameContext context, GamePlayer player) {
    listeners.values().forEach(l -> l.onJoin(context, player));
  }

  @Override
  public void postGameTick(GameContext context) {
    listeners.values().forEach(l -> l.onTick(context));
  }

  @Override
  public void postGameStart(GameContext context) {
    listeners.values().forEach(l -> l.onStart(context));
  }

  @Override
  public void postGameEnd(GameContext context) {
    listeners.values().forEach(l -> l.onEnd(context));
  }

  @Override
  public void postSubCommand(CommandSender sender, String[] args) {
    listeners.values().forEach(l -> l.onSubCommand(sender, args));
  }
}
