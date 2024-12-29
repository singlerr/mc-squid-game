package io.github.singlerr.sg.rlgl.game;

import io.github.singlerr.sg.core.context.GamePlayer;
import java.util.function.BiConsumer;
import lombok.Getter;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

public enum RLGLItemRole {
  START(Component.literal("[게임 시작]").withStyle(ChatFormatting.YELLOW),
      (ctx, player) -> {
        ctx.start();
      }),
  END(Component.literal("[게임 종료]").withStyle(ChatFormatting.YELLOW), (ctx, player) -> {
    ctx.end();
  }),
  RED_LIGHT(Component.literal("[빨간 불]").withStyle(ChatFormatting.RED),
      (ctx, player) -> {
        ctx.redLight();
      }),
  GREEN_LIGHT(Component.literal("[파란 불]").withStyle(ChatFormatting.BLUE),
      (ctx, player) -> {
        ctx.greenLight();
      });

  private static final NamespacedKey KEY = new NamespacedKey("rlgl", "role");

  private final BiConsumer<RLGLGameContext, GamePlayer> executor;
  @Getter
  private final MutableComponent displayName;

  RLGLItemRole(MutableComponent displayName, BiConsumer<RLGLGameContext, GamePlayer> executor) {
    this.displayName = displayName;
    this.executor = executor;
  }

  public static RLGLItemRole getRole(ItemStack stack) {
    ItemMeta meta = stack.getItemMeta();
    return meta.getCustomTagContainer().hasCustomTag(KEY, ItemTagType.STRING) ?
        RLGLItemRole.valueOf(meta.getCustomTagContainer().getCustomTag(KEY, ItemTagType.STRING)) :
        null;
  }

  public void execute(RLGLGameContext context, GamePlayer player) {
    this.executor.accept(context, player);
  }

  public ItemStack ofRole(ItemStack stack) {
    ItemMeta itemMeta = stack.getItemMeta();
    itemMeta.getCustomTagContainer().setCustomTag(KEY, ItemTagType.STRING, toString());
    stack.setItemMeta(itemMeta);

    return stack;
  }
}
