package io.github.singlerr.admin.network;

import io.github.singlerr.sg.core.network.Packet;
import net.minecraft.network.FriendlyByteBuf;

public final class PacketRequestInfo implements Packet {

  public static final String ID = "sgadmin:request_info";

  @Override
  public void writePayload(FriendlyByteBuf buffer) {

  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    buffer.readInt();
  }
}
