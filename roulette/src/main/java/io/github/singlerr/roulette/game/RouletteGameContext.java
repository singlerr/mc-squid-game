package io.github.singlerr.roulette.game;

import com.google.gson.Gson;
import io.github.singlerr.sg.core.context.GameContext;
import io.github.singlerr.sg.core.context.GameEventBus;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameStatus;
import io.github.singlerr.sg.core.setup.GameSettings;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.tags.ItemTagType;

public final class RouletteGameContext extends GameContext {

  private static final NamespacedKey GUN_TAG = new NamespacedKey("roulette", "gun");
  private static final Gson GSON = new Gson();

  public RouletteGameContext(Map<UUID, GamePlayer> players,
                             GameStatus status,
                             GameEventBus eventBus,
                             GameSettings settings) {
    super(players, status, eventBus, settings);
  }

  public RouletteGameSettings getGameSettings() {
    return (RouletteGameSettings) getSettings();
  }

  public Gun createGun(int bulletAmount, int realBulletAmount) {
    Gun g = new Gun();
    if (!g.reload(bulletAmount, realBulletAmount)) {
      return null;
    }

    return g;
  }

  public ItemStack createGun(Gun gun) {
    ItemStack stack = new ItemStack(getGameSettings().getGunType());
    ItemMeta itemMeta = stack.getItemMeta();
    itemMeta.getCustomTagContainer().setCustomTag(GUN_TAG, ItemTagType.STRING, GSON.toJson(gun));
    itemMeta.displayName(Component.text(getGameSettings().getGunName()).style(Style.style(
        NamedTextColor.RED)));
    stack.setItemMeta(itemMeta);

    return stack;
  }

  public Gun getGun(ItemStack stack) {
    ItemMeta itemMeta = stack.getItemMeta();
    if (!itemMeta.getCustomTagContainer().hasCustomTag(GUN_TAG, ItemTagType.STRING)) {
      return null;
    }
    return GSON.fromJson(itemMeta.getCustomTagContainer().getCustomTag(GUN_TAG, ItemTagType.STRING),
        Gun.class);
  }

  public void playReloadingAnimation(Player player) {

  }

  public void shot(Player player, Gun gun) {

  }
}
