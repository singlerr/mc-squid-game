package io.github.singlerr.sg.core.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.slf4j.helpers.MessageFormatter;

public abstract class InteractableListener implements Listener {

  protected boolean errorCallback(Player sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }

  protected boolean successCallback(Player sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }

  protected boolean infoCallback(Player sender, String message, Object... args) {
    sender.sendMessage(Component.text(MessageFormatter.basicArrayFormat(message, args)));
    return false;
  }
}
