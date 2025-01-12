package io.github.singlerr.sg.core.utils;

import fr.skytasul.glowingentities.GlowingEntities;
import io.github.singlerr.sg.core.GameCore;
import io.github.singlerr.sg.core.context.GamePlayer;
import io.github.singlerr.sg.core.context.GameRole;
import io.github.singlerr.sg.core.context.Gender;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import lombok.Setter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.exception.MineSkinException;
import net.skinsrestorer.api.property.InputDataResult;
import net.skinsrestorer.api.property.SkinVariant;
import net.skinsrestorer.api.storage.PlayerStorage;
import net.skinsrestorer.api.storage.SkinStorage;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

@Slf4j
@UtilityClass
public class PlayerUtils {

  private SecureRandom random = new SecureRandom();
  @Setter
  private GlowingEntities glowingManager;

  public boolean contains(Collection<Player> players, Player player) {
    return players.stream().anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
  }

  public boolean contains(Collection<GamePlayer> players, GamePlayer player) {
    return players.stream()
        .anyMatch(p -> p.getId().equals(player.getId()));
  }

  public void changeSkin(Player player, GameRole role, Gender gender) {
    if (role.getLevel() <= GameRole.TROY.getLevel()) {
      List<String> urls = GameCore.getInstance().getPlayerSkinUrl(gender == Gender.MALE);
      int idx = random.nextInt(urls.size());
      changeSkin(player, urls.get(idx), gender == Gender.FEMALE);
    }
  }

  public void disableGlowing(Entity entity, Collection<Player> receivers) {

    for (Player receiver : receivers) {
      try {
        glowingManager.unsetGlowing(entity, receiver);
      } catch (Exception e) {
        log.error("Failed to enable glowing", e);
      }
    }
  }

  public void enableGlowing(Collection<Player> entity, Collection<Player> receivers,
                            ChatColor color) {
    for (Player player : entity) {
      enableGlowing(player, receivers, color);
    }
  }

  public void enableEntityGlowing(Collection<Entity> entity, Collection<Player> receivers,
                                  ChatColor color) {
    for (Entity player : entity) {
      enableGlowing(player, receivers, color);
    }
  }

  public void disableGlowing(Collection<Player> entity, Collection<Player> receivers) {
    for (Player player : entity) {
      disableGlowing(player, receivers);
    }
  }

  public void disableEntityGlowing(Collection<Entity> entity, Collection<Player> receivers) {
    for (Entity player : entity) {
      disableGlowing(player, receivers);
    }
  }


  public void enableGlowing(Entity entity, Collection<Player> receivers, ChatColor color) {
    for (Player receiver : receivers) {
      try {
        glowingManager.setGlowing(entity, receiver, color);
      } catch (Exception e) {
        log.error("Failed to enable glowing", e);
      }
    }
  }

  public ClientboundSetEntityDataPacket createChangeCustomNamePacket(Player player,
                                                                     Component displayName) {
    List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
    eData.add(SynchedEntityData.DataValue.create(
        new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
        Optional.of(net.minecraft.network.chat.Component.literal(
            PlainTextComponentSerializer.plainText().serialize(displayName)))));
    return new ClientboundSetEntityDataPacket(player.getEntityId(), eData);
  }

  public ClientboundSetEntityDataPacket createChangeCustomNamePacket(Player player,
                                                                     String displayName) {
    List<SynchedEntityData.DataValue<?>> eData = new ArrayList<>();
    eData.add(SynchedEntityData.DataValue.create(
        new EntityDataAccessor<>(2, EntityDataSerializers.OPTIONAL_COMPONENT),
        Optional.ofNullable(net.minecraft.network.chat.Component.literal(displayName))));
    return new ClientboundSetEntityDataPacket(player.getEntityId(), eData);
  }

  public void loadSkin(String skinUrl, boolean slim) {
    SkinsRestorer api = SkinsRestorerProvider.get();
    SkinStorage storage = api.getSkinStorage();
    try {
      Optional<InputDataResult> result =
          storage.findOrCreateSkinData(skinUrl, slim ? SkinVariant.SLIM : SkinVariant.CLASSIC);
      if (result.isEmpty()) {
        log.error("Failed to load skin {}", skinUrl);
      } else {
        log.info("Loaded skin {}", skinUrl);
      }
    } catch (DataRequestException | MineSkinException e) {
      log.error("Failed to apply skin", e);
    }
  }

  public void changeSkin(Player player, String skinUrl, boolean slim) {
    SkinsRestorer api = SkinsRestorerProvider.get();
    SkinStorage storage = api.getSkinStorage();
    try {
      Optional<InputDataResult> result =
          storage.findOrCreateSkinData(skinUrl, slim ? SkinVariant.SLIM : SkinVariant.CLASSIC);
      if (result.isEmpty()) {
        log.error("Failed to apply skin {}", skinUrl);
        return;
      }

      PlayerStorage playerStorage = api.getPlayerStorage();
      playerStorage.setSkinIdOfPlayer(player.getUniqueId(), result.get().getIdentifier());

      api.getSkinApplier(Player.class).applySkin(player);
      log.info("Changed {} skin to {}", player.getName(), skinUrl);
    } catch (DataRequestException | MineSkinException e) {
      log.error("Failed to apply skin", e);
    }
  }
}
