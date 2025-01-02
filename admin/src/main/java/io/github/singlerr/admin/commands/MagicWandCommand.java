package io.github.singlerr.admin.commands;

import io.github.singlerr.admin.CommandContexts;
import io.github.singlerr.sg.core.utils.Transform;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.helpers.MessageFormatter;

public final class MagicWandCommand implements CommandExecutor {

  private static final Pattern VECTOR_PATTERN = Pattern.compile(
      "\\(\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*\\)");
  private static final Pattern QUATERNION_PATTERN = Pattern.compile(
      "\\(\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*,\\s*(-?[0-9]+(?:\\.[0-9]+)?)\\s*\\)");

  @Override
  public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
    if (args.length > 0) {
      Player player = (Player) sender;
      if (args[0].equalsIgnoreCase("cancel")) {
        CommandContexts.end(player.getUniqueId());
        sender.sendMessage(Component.text("비활성화").style(Style.style(NamedTextColor.YELLOW)));
        return false;
      }

      String modelLocation = args[0];

      Vector3f t = args.length > 1 ? parseVector(args[1]) : null;
      Quaternionf r = args.length > 2 ? parseQuaternion(args[2]) : null;
      Vector3f s = args.length > 3 ? parseVector(args[3]) : null;

      CommandContexts.begin(player.getUniqueId(),
          new CommandContexts.Context(modelLocation, new Transform(t, r, s)));
      sender.sendMessage(Component.text(
          MessageFormatter.basicArrayFormat("이제 블레이즈 막대로 우클릭하여 다음 모델 생성: {}, {}",
              new Object[] {modelLocation})).style(
          Style.style(NamedTextColor.YELLOW)));
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
