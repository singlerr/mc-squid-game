package io.github.singlerr.trolley.network;

import io.github.singlerr.sg.core.network.Packet;
import lombok.Data;
import net.minecraft.network.FriendlyByteBuf;

@Data
public final class PacketIntermissionResult implements Packet {

  public static final String ID = "intermission:intermission_result";

  private boolean success;

  @Override
  public void writePayload(FriendlyByteBuf buffer) {

  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {

  }
}
