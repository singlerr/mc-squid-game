package io.github.singlerr.admin.commands;

import io.github.singlerr.sg.core.utils.PlayerUtils;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class MagicWandCommand implements CommandExecutor {

  private static final Pattern VECTOR_PATTERN = Pattern.compile(
      "\\(\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*\\)");
  private static final Pattern QUATERNION_PATTERN = Pattern.compile(
      "\\(\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*\\)");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    Player p = Bukkit.getPlayer(args[0]);
    if (p != null) {
      PlayerUtils.enableGlowing(p, List.of((Player) sender), ChatColor.AQUA);
    }
    return false;
  }

  private Vector3f parseVector(String args) {
    Matcher m = VECTOR_PATTERN.matcher(args);
    if (!m.matches()) {
      return null;
    }
    return new Vector3f(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)),
        Float.parseFloat(m.group(3)));
  }

  private Quaternionf parseQuaternion(String args) {
    Matcher m = QUATERNION_PATTERN.matcher(args);
    if (!m.matches()) {
      return null;
    }
    return new Quaternionf(Float.parseFloat(m.group(1)), Float.parseFloat(m.group(2)),
        Float.parseFloat(m.group(3)), Float.parseFloat(m.group(4)));
  }
}
