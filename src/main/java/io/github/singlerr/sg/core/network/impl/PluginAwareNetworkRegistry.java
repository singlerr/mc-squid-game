package io.github.singlerr.sg.core.network.impl;

import io.github.singlerr.sg.core.network.NetworkRegistry;
import io.github.singlerr.sg.core.network.Packet;
import io.github.singlerr.sg.core.network.PacketChannel;
import io.github.singlerr.sg.core.network.PacketRegistry;
import io.github.singlerr.sg.core.registry.Registry;
import io.github.singlerr.sg.core.registry.impl.RegistryFactory;
import java.util.Collection;
import org.bukkit.plugin.Plugin;

public final class PluginAwareNetworkRegistry implements NetworkRegistry {

  private final PacketChannel channel;
  private final Plugin plugin;

  private final Registry<PacketRegistry<? extends Packet>> internal;

  public PluginAwareNetworkRegistry(Plugin plugin) {
    this.plugin = plugin;
    this.channel = new PacketChannel(plugin);
    this.internal = RegistryFactory.defaultFactory().create("network_internal");
  }

  @Override
  public PacketChannel getChannel() {
    return channel;
  }

  @Override
  public String getId() {
    return "network_registry";
  }

  @Override
  public void register(String id, PacketRegistry<? extends Packet> reg) {
    internal.register(id, reg);
  }

  @Override
  public PacketRegistry<? extends Packet> getById(String id) {
    return this.channel.getPacketRegistration(id);
  }

  @Override
  public Collection<PacketRegistry<? extends Packet>> values() {
    return this.channel.getRegistries();
  }

  @Override
  public Collection<String> keys() {
    return this.channel.getIds();
  }

  public void registerToMessengers() {
    this.channel.register(r -> {
      for (String key : internal.keys()) {
        r.register(key, internal.getById(key));
      }
    }, id -> {
      plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, id);
      plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, id, channel);
    });
  }
}
