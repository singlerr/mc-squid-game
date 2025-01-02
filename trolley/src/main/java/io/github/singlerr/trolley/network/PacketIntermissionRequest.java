package io.github.singlerr.trolley.network;

import io.github.singlerr.sg.core.network.Packet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;

@AllArgsConstructor
@NoArgsConstructor
@Data
public final class PacketIntermissionRequest implements Packet {

  public static final String ID = "intermission:request_intermission";

  private long duration;
  private float startAngle;
  private float sweepAngle;


  @Override
  public void writePayload(FriendlyByteBuf buffer) {
    buffer.writeLong(duration);
    buffer.writeFloat(startAngle);
    buffer.writeFloat(sweepAngle);
  }

  @Override
  public void readPayload(FriendlyByteBuf buffer) {
    this.duration = buffer.readLong();
    this.startAngle = buffer.readFloat();
    this.sweepAngle = buffer.readFloat();
  }
}
