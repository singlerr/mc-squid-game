package io.github.singlerr.sg.core.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

@Slf4j
public final class PacketChannel implements PluginMessageListener {

  private final Plugin plugin;
  private final BiMap<String, Class<? extends Packet>> packetMapper;

  public PacketChannel(Plugin plugin) {
    this.plugin = plugin;
    this.packetMapper = HashBiMap.create();
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    Class<?> packetCls = packetMapper.get(channel);
    if (packetCls == null) {
      return;
    }
    ByteArrayDataInput buffer = ByteStreams.newDataInput(message);
    try {
      Packet packet = (Packet) packetCls.getConstructor().newInstance();
      packet.readPayload(buffer);
      packet.handle(player);
    } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
             InvocationTargetException e) {
      log.error("Cannot instantiate {}!", packetCls.getSimpleName(), e);
    }
  }

  public Class<? extends Packet> getPacketRegistration(String id) {
    return this.packetMapper.get(id);
  }

  public Set<Class<? extends Packet>> getRegistries() {
    return this.packetMapper.values();
  }

  public Set<String> getIds() {
    return this.packetMapper.keySet();
  }

  public void register(Consumer<Registry> registryConsumer, Consumer<String> channelRegistry) {
    Registry registry = new Registry(this.packetMapper);
    registryConsumer.accept(registry);
    registry.registry.keySet().forEach(channelRegistry);
  }

  public void sendTo(Player player, Packet packet) {
    Class<? extends Packet> cls = packet.getClass();
    String id = packetMapper.inverse().get(cls);

    if (id == null) {
      log.error("Packet mapping for {} couldn't be found!", cls.getSimpleName());
      return;
    }

    ByteArrayDataOutput buffer = ByteStreams.newDataOutput();
    packet.writePayload(buffer);

    player.sendPluginMessage(plugin, id, buffer.toByteArray());
  }


  public static final class Registry {

    private final BiMap<String, Class<? extends Packet>> registry;

    public Registry(BiMap<String, Class<? extends Packet>> registry) {
      this.registry = registry;
    }

    public void register(String name, Class<? extends Packet> packet) {
      this.registry.put(name, packet);
    }
  }

}
