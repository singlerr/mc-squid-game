package io.github.singlerr.sg.core.network;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet {

  void writePayload(FriendlyByteBuf buffer);

  void readPayload(FriendlyByteBuf buffer);

}
