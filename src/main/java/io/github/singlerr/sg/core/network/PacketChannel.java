package io.github.singlerr.sg.core.network;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.netty.buffer.Unpooled;
import java.util.Set;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

@Slf4j
public final class PacketChannel implements PluginMessageListener {

  private final Plugin plugin;
  private final BiMap<String, PacketRegistry<? extends Packet>> packetMapper;

  public PacketChannel(Plugin plugin) {
    this.plugin = plugin;
    this.packetMapper = HashBiMap.create();
  }

  @Override
  public void onPluginMessageReceived(String channel, Player player, byte[] message) {
    PacketRegistry<? extends Packet> reg = packetMapper.get(channel);
    if (reg == null) {
      return;
    }
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.wrappedBuffer(message));
    handlePacket(player, buffer, reg);
  }

  private <T extends Packet> void handlePacket(Player player, FriendlyByteBuf buffer,
                                               PacketRegistry<T> registry) {
    T packet = registry.getPacketType().getFactory().get();
    packet.readPayload(buffer);
    registry.getHandler().handle(packet, player);
  }

  public <T extends Packet> PacketRegistry<T> getPacketRegistration(String id) {
    if (!packetMapper.containsKey(id)) {
      return null;
    }

    return (PacketRegistry<T>) packetMapper.get(id);
  }

  public Set<PacketRegistry<? extends Packet>> getRegistries() {
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

  private <T extends Packet> PacketRegistry<T> createInversedKey(Class<T> cls) {
    return new PacketRegistry<>(new PacketType<>(cls, null), null);
  }

  public <T extends Packet> void sendTo(Player player, T packet) {
    Class<T> cls = (Class<T>) packet.getClass();
    String id = packetMapper.inverse().get(createInversedKey(cls));

    if (id == null) {
      log.error("Packet mapping for {} couldn't be found!", cls.getSimpleName());
      return;
    }

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    packet.writePayload(buffer);

    byte[] arr = new byte[buffer.writerIndex()];
    buffer.readBytes(arr);
    player.sendPluginMessage(plugin, id, arr);
  }

  public <T extends Packet> void sendToAll(T packet) {
    Class<T> cls = (Class<T>) packet.getClass();
    String id = packetMapper.inverse().get(createInversedKey(cls));

    if (id == null) {
      log.error("Packet mapping for {} couldn't be found!", cls.getSimpleName());
      return;
    }

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    packet.writePayload(buffer);

    byte[] arr = new byte[buffer.writerIndex()];
    buffer.readBytes(arr);
    for (Player player : Bukkit.getServer().getOnlinePlayers()) {
      player.sendPluginMessage(plugin, id, arr);
    }
  }


  public static final class Registry {

    private final BiMap<String, PacketRegistry<? extends Packet>> registry;

    public Registry(BiMap<String, PacketRegistry<? extends Packet>> registry) {
      this.registry = registry;
    }

    public void register(String name, PacketRegistry<? extends Packet> packet) {
      this.registry.put(name, packet);
    }
  }

}
