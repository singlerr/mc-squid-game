package io.github.singlerr.sg.core.network;

import io.github.singlerr.sg.core.registry.Registry;

public interface NetworkRegistry extends Registry<Class<? extends Packet>> {

  PacketChannel getChannel();

}
